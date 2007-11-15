package org.zfin.publication.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.Publication;
import org.zfin.marker.Marker;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.presentation.LookupStrings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Action class that serves the page that displays a list of publications.
 */
public class PublicationSearchResultController extends AbstractCommandController {

    private static Logger LOG = Logger.getLogger(PublicationSearchResultController.class);
    private static AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();

    public PublicationSearchResultController() {
        setCommandClass(PublicationSearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        PublicationSearchBean form = (PublicationSearchBean) command;

        PublicationSearchResultController.LOG.info("Start Publication Search Controller");

        AnatomyItem ai = ar.loadAnatomyItem(form.getAnatomyItem());
        form.setAnatomyItem(ai);
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        // ToDo All this stuff about pagination and ordering should be more streamlined
        // in a framework
        pr.setFirstRow(form.getFirstRecord());
        pr.setMaxDisplayRows(form.getMaxDisplayRecords());
        pr.setUsePagination(true);
        pr.removeOrderByFields();
        pr.addOrdering("publication.publicationDate asc");
        Marker marker = pr.getMarkerByZdbID(form.getMarker().getZdbID());
        form.setMarker(marker);
        List<Publication> publications = marker.getPublications(form.getAnatomyItem().getZdbID());
        Collections.sort(publications, new SortPublicationResults("date", true));
        form.setPublications(publications);
        int totalNumberOfPublication = pr.getNumberOfExpressedGenePublications(form.getMarker().getZdbID(), form.getAnatomyItem().getZdbID() );
        form.setTotalRecords(totalNumberOfPublication);
        form.addRequestParameter("anatomyItem.zdbID", ai.getZdbID());
        form.addRequestParameter("marker.zdbID", marker.getZdbID());

        return new ModelAndView("publication-search_result.page", LookupStrings.FORM_BEAN, form);
    }

}
