package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Action class that serves the anatomy term detail page.
 */
public class HighQualityProbesController extends AbstractCommandController {

    private static Logger LOG = Logger.getLogger(HighQualityProbesController.class);
    private static AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();

    public HighQualityProbesController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        AnatomySearchBean anatomyForm = (AnatomySearchBean) command;

        HighQualityProbesController.LOG.info("Start High Quality Probes  Controller");
        AnatomyItem anatomyTerm = ar.getAnatomyTermByID(anatomyForm.getAnatomyItem().getZdbID());
        if (anatomyTerm == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, anatomyForm.getAnatomyItem().getZdbID());

        anatomyForm.setAnatomyItem(anatomyTerm);

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        int numberOfPublications = pr.getNumberOfPublications(anatomyTerm.getName());
        anatomyForm.setNumberOfPublications(numberOfPublications);

        List<Publication> qualityPubs = pr.getHighQualityProbePublications(anatomyTerm);
        anatomyForm.setQualityProbePublications(qualityPubs);

        // Get the genes of the high-quality probes
        List<HighQualityProbe> hqp = pr.getHighQualityProbeNames(anatomyTerm);
        anatomyForm.setHighQualityProbeGenes(hqp);
        createFigureStatisticsOnHQP(anatomyForm);

        int numberOfHighQualityProbes = pr.getNumberOfHighQualityProbes(anatomyTerm);
        anatomyForm.setNumberOfHighQualityProbes(numberOfHighQualityProbes);

        return new ModelAndView("high-quality-probes.page", LookupStrings.FORM_BEAN, anatomyForm);
    }

    // ToDo: This logic belongs into the repository
    private void createFigureStatisticsOnHQP(AnatomySearchBean anatomyForm) {
        List<HighQualityProbe> hqpProbes = anatomyForm.getHighQualityProbeGenes();
        List<Publication> pubs = anatomyForm.getQualityProbePublications();
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        if (hqpProbes != null) {
            for (HighQualityProbe probe : hqpProbes) {
                if (pubs != null) {
                    for (Publication pub : pubs) {
                        List<Figure> figures = pr.getFiguresByProbeAndPublication(probe.getSubGene().getZdbID(), pub.getZdbID());
                        // ToDO: This assumes there is only a single publication for HQP pubc. That may not be true...
                        if (figures != null && figures.size() > 0) {
                            probe.setFigures(figures);
                            probe.setPublication(pub);
                        }
                    }
                }
            }
        }
    }

}
