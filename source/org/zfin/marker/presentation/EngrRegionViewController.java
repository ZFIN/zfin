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
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
@RequestMapping("/marker")
public class EngrRegionViewController {

    private Logger logger = Logger.getLogger(EngrRegionViewController.class);

//    @Autowired
//    private ExpressionService expressionService ;

    @Autowired
    private MarkerRepository markerRepository ;
    
    @Autowired
    private EfgViewController efgViewController;

    @RequestMapping(value ="/engregion/view/{zdbID}")
    public String getView(
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


//      CITATIONS
        markerBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(region.getZdbID()));
        markerBean.setSequenceInfo(MarkerService.getSequenceInfoSummary(region));
        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.EREGION.getTitleString() + region.getAbbreviation());

        return "marker/engrRegion-view.page";
    }
}