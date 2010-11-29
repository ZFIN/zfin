package org.zfin.marker.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerService;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class SequenceViewController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        // set base bean
        MarkerBean markerBean = new MarkerBean();

        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID);
        logger.info("zdbID: " + zdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        markerBean.setMarker(gene);

        //setting supporting sequences
        SequenceInfo sequenceInfo = MarkerService.getSequenceInfo(gene);
        markerBean.setSequenceInfo(sequenceInfo);


//        // setting clone relationshi8ps
        RelatedMarkerDisplay cloneRelationships = MarkerService.getRelatedMarkerDisplay(gene);
        markerBean.setMarkerRelationships(cloneRelationships);


        ModelAndView modelAndView = new ModelAndView("marker/sequence-view.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, markerBean);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, "Sequences for Gene: " + gene.getAbbreviation());

        return modelAndView;
    }
}