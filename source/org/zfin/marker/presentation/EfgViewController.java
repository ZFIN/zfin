package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Controller
@RequestMapping("/marker")
public class EfgViewController {

    private Logger logger = Logger.getLogger(EfgViewController.class);

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @RequestMapping(value = "/efg/view/{zdbID}")
    public String getView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        // set base bean
        MarkerBean markerBean = new MarkerBean();

        zdbID = markerService.getActiveMarkerID(zdbID);
        logger.info("zdbID: " + zdbID);
        Marker efg = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + efg);
        markerBean.setMarker(efg);

        // not used, too much stuff excluded
//        MarkerService.createDefaultViewForMarker(markerBean);

        markerBean.setMarkerTypeDisplay(MarkerService.getMarkerTypeString(efg));
        markerBean.setPreviousNames(markerRepository.getPreviousNamesLight(efg));
        markerBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID));

        // EXPRESSION SECTION
        markerBean.setMarkerExpression(expressionService.getExpressionForEfg(efg));

        // (CONSTRUCTS)
        populateConstructList(markerBean, efg);

        // (Antibodies)
        markerBean.setRelatedAntibodies(markerRepository
                .getRelatedMarkerDisplayForTypes(efg, true
                        , MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));


//      CITATIONS
        markerBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(efg.getZdbID()));
        markerBean.setSequenceInfo(MarkerService.getSequenceInfoSummary(efg));
        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.EFG.getTitleString() + efg.getAbbreviation());

        return "marker/efg-view.page";
    }

    protected void populateConstructList(MarkerBean markerBean, Marker efg) {
        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION);
        Set<Marker> markerSet = new TreeSet<>();
        PaginationResult<Marker> relatedMarker = MarkerService.getRelatedMarker(efg, types, 7);
        markerSet.addAll(relatedMarker.getPopulatedResults());
        markerBean.setConstructs(markerSet);
        markerBean.setNumberOfConstructs(relatedMarker.getTotalCount());
    }

    @RequestMapping(value = "/efg/constructs/{zdbID}")
    public String getAllConstructs(Model model,
                                   @PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        MarkerBean markerBean = new MarkerBean();
        Marker efg = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + efg);
        markerBean.setMarker(efg);
        // (CONSTRUCTS)
        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION);
        Set<Marker> markerSet = new TreeSet<>();
        // get all constructs
        PaginationResult<Marker> relatedMarker = MarkerService.getRelatedMarker(efg, types, -1);
        markerSet.addAll(relatedMarker.getPopulatedResults());
        markerBean.setConstructs(markerSet);
        markerBean.setNumberOfConstructs(relatedMarker.getTotalCount());
        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        return "marker/efg-all-constructs.ajax";
    }
}