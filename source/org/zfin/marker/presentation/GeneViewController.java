package org.zfin.marker.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.zfin.expression.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerService;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class GeneViewController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws Exception {
        // set base bean
        MarkerBean markerBean = new MarkerBean();

        String zdbID = httpServletRequest.getParameter(LookupStrings.ZDB_ID);
        logger.info("zdbID: " + zdbID);
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        logger.info("gene: " + gene);
        markerBean.setMarker(gene);

        markerBean.setMarkerExpression(ExpressionService.getExpressionForMarker(gene));
//
//        // setting clone relationshi8ps
        RelatedMarkerDisplay cloneRelationships = MarkerService.getRelatedMarkerDisplay(gene);
        markerBean.setMarkerRelationships(cloneRelationships);
//

        //get the protein summary pages
        markerBean.setProteinProductDBLinkDisplay(MarkerService.getProteinProductDBLinks(gene));

        //setting supporting sequences
        SequenceInfo sequenceInfo = MarkerService.getSequenceInfo(gene);
        markerBean.setSequenceInfo(sequenceInfo);
//
//        markerBean.setSummaryDBLinkDisplay(MarkerService.getSummaryPages(gene));
//
//        // setting other clones
////        MarkerDBLinkList otherClones = RepositoryFactory.getSequenceRepository().getSummaryMarkerDBLinksForMarker( clone) ;
////        cloneBean.setOtherClones(otherClones);

        // set mapping data
        markerBean.setMappedMarkerBean(MarkerService.getMappedMarkers(gene));
//
//        // check whether we are a thisse probe
//        markerBean.setThisseProbe(ExpressionService.isThisseProbe(gene));
//
//        // setting publications
        markerBean.setNumPubs(RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForMarker(
                gene, 0).getTotalCount());

        ModelAndView modelAndView = new ModelAndView("marker/gene-view.page");
        modelAndView.addObject(LookupStrings.FORM_BEAN, markerBean);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, Area.GENE.getTitleString() + gene.getAbbreviation());

        return modelAndView;
    }
}