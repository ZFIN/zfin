package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action class that serves the Thisse probes pages.
 */
public class HighQualityProbesController extends AbstractCommandController {

    private static Logger LOG = Logger.getLogger(HighQualityProbesController.class);
    private static AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();

    public HighQualityProbesController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        AnatomySearchBean anatomyForm = (AnatomySearchBean) command;

        HighQualityProbesController.LOG.info("Start High Quality Probes Controller");
        Term term = OntologyManager.getInstance().getTermByID(Ontology.ANATOMY, anatomyForm.getAnatomyItem().getZdbID());
        if (term == null)
            return new ModelAndView(LookupStrings.RECORD_NOT_FOUND_PAGE, LookupStrings.ZDB_ID, anatomyForm.getAnatomyItem().getZdbID());

        anatomyForm.setAoTerm(term);

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        anatomyForm.setQueryString(request.getQueryString());
        anatomyForm.setRequestUrl(request.getRequestURL());

        PaginationResult<HighQualityProbe> hqp = markerRepository.getHighQualityProbeStatistics(term, anatomyForm, false);
        anatomyForm.setHighQualityProbeGenes(hqp.getPopulatedResults());
        anatomyForm.setNumberOfHighQualityProbes(hqp.getTotalCount());
        anatomyForm.setTotalRecords(hqp.getTotalCount());

        return new ModelAndView("high-quality-probes.page", LookupStrings.FORM_BEAN, anatomyForm);
    }

}
