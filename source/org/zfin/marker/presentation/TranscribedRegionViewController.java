package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
@RequestMapping("/marker")
public class TranscribedRegionViewController {

    private Logger logger = Logger.getLogger(TranscribedRegionViewController.class);

//    @Autowired
//    private ExpressionService expressionService ;

    @Autowired
    private MarkerRepository markerRepository ;
    
    @Autowired
    private EfgViewController efgViewController;

    @RequestMapping(value ="/transcribedregion/view/{zdbID}")
    public String getGeneView(
            Model model
            ,@PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        MarkerBean markerBean = new MarkerBean();

        logger.info("zdbID: " + zdbID);
        Marker region = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + region);
        markerBean.setMarker(region);

        // not used, too much stuff excluded
//        MarkerService.createDefaultViewForMarker(markerBean);

        markerBean.setMarkerTypeDisplay(MarkerService.getMarkerTypeString(region));
        markerBean.setPreviousNames(markerRepository.getPreviousNamesLight(region));
        markerBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID)) ;

        // EXPRESSION SECTION
//        markerBean.setMarkerExpression(expressionService.getExpressionForEfg(region));

        // (CONSTRUCTS)
        efgViewController.populateConstructList(markerBean, region);

        // (Antibodies)
//        markerBean.setRelatedAntibodies(markerRepository
//                .getRelatedMarkerDisplayForTypes(region, true
//                        , MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));

        markerBean.setGeneOntologyOnMarkerBeans(MarkerService.getGeneOntologyOnMarker(region));
//      CITATIONS
        markerBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(region.getZdbID()));
        markerBean.setSequenceInfo(MarkerService.getSequenceInfoSummary(region));
        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute("markerHistoryReasonCodes", MarkerHistory.Reason.values());
       // model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.REGION.getTitleString() + region.getAbbreviation());

        return "marker/region-view.page";
    }
}