package org.zfin.marker.presentation;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;
import org.zfin.marker.Clone;
import org.zfin.marker.MarkerService;
import org.zfin.marker.Marker;
import org.zfin.expression.ExpressionService;
import org.zfin.mapping.presentation.MappedMarkerBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CloneViewController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        // set base bean
        CloneViewBean cloneViewBean = new CloneViewBean() ;

        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID) ;
        logger.info("zdbID: " + zdbID);
        Clone clone = RepositoryFactory.getMarkerRepository().getCloneById(zdbID) ;
        logger.info("clone: " + clone);
        cloneViewBean.setMarker(clone);

        cloneViewBean.setMarkerExpression(ExpressionService.getExpressionForMarker(clone));

        // setting clone relationshi8ps
        RelatedMarkerDisplay cloneRelationships = MarkerService.getRelatedMarkerDisplay(clone);
        cloneViewBean.setMarkerRelationships(cloneRelationships);


        //setting supporting sequences
        SequenceInfo sequenceInfo = MarkerService.getSequenceInfo(clone);
        cloneViewBean.setSequenceInfo(sequenceInfo);

        cloneViewBean.setSummaryDBLinkDisplay(MarkerService.getSummaryPages(clone));

        // setting other clones
//        MarkerDBLinkList otherClones = RepositoryFactory.getSequenceRepository().getSummaryMarkerDBLinksForMarker( clone) ;
//        cloneBean.setOtherClones(otherClones);

        // set mapping data
        cloneViewBean.setMappedMarkerBean(MarkerService.getMappedMarkers(clone)) ; 

        // check whether we are a thisse probe
        cloneViewBean.setThisseProbe(ExpressionService.isThisseProbe(clone)) ;

        // setting publications
        cloneViewBean.setNumPubs(RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForMarker(
                clone,0).getTotalCount());

        ModelAndView modelAndView = new ModelAndView("clone-view.page") ;
        modelAndView.addObject(LookupStrings.FORM_BEAN, cloneViewBean) ;
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, clone.getAbbreviation()) ;

        return modelAndView ;
    }
}