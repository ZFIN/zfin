package org.zfin.marker.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.seo.CanonicalLinkConfig;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 */
@Controller
@RequestMapping("/marker")
public class EngrRegionViewController {

    private Logger logger = LogManager.getLogger(EngrRegionViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @Autowired
    private EfgViewController efgViewController;

    @RequestMapping(value = "/eregion/view/{zdbID}")
    public String getView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        CanonicalLinkConfig.addCanonicalIfFound(model);
        // set base bean
        MarkerBean markerBean = new MarkerBean();

        zdbID = markerService.getActiveMarkerID(zdbID);
        logger.info("zdbID: " + zdbID);
        Marker region = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + region);
        markerBean.setMarker(region);

        // not used, too much stuff excluded
//        MarkerService.createDefaultViewForMarker(markerBean);

        markerBean.setMarkerTypeDisplay(MarkerService.getMarkerTypeString(region));
        markerBean.setPreviousNames(markerRepository.getPreviousNamesLight(region));
        markerBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID));

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

        return "marker/eregion/eregion-view";
    }

    @RequestMapping("/view-all-engineered-regions/")
    public String viewAllEngineeredRegions(Model model) {
        List<Marker> engineeredRegions = markerRepository.getAllEngineeredRegions();
        model.addAttribute("engineeredRegions", engineeredRegions);
        return "marker/view-all-engineered-regions";
    }
}