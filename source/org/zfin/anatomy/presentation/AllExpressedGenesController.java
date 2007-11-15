package org.zfin.anatomy.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.marker.presentation.ExpressedGeneDisplay;
import org.zfin.marker.MarkerStatistic;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Action class that serves the page that displays all expressed genes in a given anatomical structure.
 */
public class AllExpressedGenesController extends AbstractCommandController {

    private static Logger LOG = Logger.getLogger(AllExpressedGenesController.class);
    private static AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();

    public AllExpressedGenesController() {
        setCommandClass(AnatomySearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        AnatomySearchBean form = (AnatomySearchBean) command;

        AllExpressedGenesController.LOG.info("Start All Expressed Genes Controller");
        AnatomyItem anatomyTerm = AllExpressedGenesController.ar.loadAnatomyItem(form.getAnatomyItem());
        form.setAnatomyItem(anatomyTerm);

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        int markerCount = pr.getAllExpressedMarkersCount(anatomyTerm);
        form.setTotalRecords(markerCount);
        List<MarkerStatistic> markers = pr.getAllExpressedMarkers(anatomyTerm, form.getFirstRecord(), form.getMaxDisplayRecords());
        form.addRequestParameter("anatomyItem.zdbID", anatomyTerm.getZdbID());

        List<ExpressedGeneDisplay> expressedGenes = createFigureStatistics(markers);
        form.setAllExpressedMarkers(expressedGenes);

        ModelAndView modelAndView = new ModelAndView("all-expressed-genes.page", LookupStrings.FORM_BEAN, form);
        return modelAndView;
    }

    protected static List<ExpressedGeneDisplay> createFigureStatistics(List<MarkerStatistic> markers) {
        List<ExpressedGeneDisplay> genes = new ArrayList<ExpressedGeneDisplay>();
        if (markers != null) {
            for (MarkerStatistic marker : markers) {
                ExpressedGeneDisplay expressedGene = new ExpressedGeneDisplay(marker);
                genes.add(expressedGene);
            }
        }
        return genes;
    }

}
