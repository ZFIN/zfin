package org.zfin.marker.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.expression.ExpressionService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Clone;
import org.zfin.marker.MarkerService;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class CloneViewController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        // set base bean
        CloneBean cloneBean = new CloneBean();

        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID);
        logger.info("zdbID: " + zdbID);
        Clone clone = RepositoryFactory.getMarkerRepository().getCloneById(zdbID);
        logger.info("clone: " + clone);
        cloneBean.setMarker(clone);

        cloneBean.setMarkerExpression(ExpressionService.getExpressionForMarker(clone));

        // setting clone relationshi8ps
        RelatedMarkerDisplay cloneRelationships = MarkerService.getRelatedMarkerDisplay(clone);
        cloneBean.setMarkerRelationships(cloneRelationships);


        //setting supporting sequences
        SequenceInfo sequenceInfo = MarkerService.getSequenceInfo(clone);
        cloneBean.setSequenceInfo(sequenceInfo);

        cloneBean.setSummaryDBLinkDisplay(MarkerService.getSummaryPages(clone));

        // setting other clones
//        MarkerDBLinkList otherClones = RepositoryFactory.getSequenceRepository().getSummaryMarkerDBLinksForMarker( clone) ;
//        cloneBean.setOtherClones(otherClones);

        // set mapping data
        cloneBean.setMappedMarkerBean(MarkerService.getMappedMarkers(clone));

        // check whether we are a thisse probe
        cloneBean.setThisseProbe(ExpressionService.isThisseProbe(clone));

        // setting publications
        cloneBean.setNumPubs(RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForMarker(
                clone, 0).getTotalCount());

        ModelAndView modelAndView = new ModelAndView("clone-view.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, cloneBean);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, clone.getAbbreviation());

        return modelAndView;
    }
}