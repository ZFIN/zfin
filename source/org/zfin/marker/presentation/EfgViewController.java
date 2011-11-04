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
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.Set;

/**
 */
@Controller
public class EfgViewController {

    private Logger logger = Logger.getLogger(EfgViewController.class);

    @Autowired
    private ExpressionService expressionService ;

    @Autowired
    private MarkerRepository markerRepository ;

    @RequestMapping(value ="/efg/view/{zdbID}")
    public String getView(
            Model model
            ,@PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        MarkerBean markerBean = new MarkerBean();

        logger.info("zdbID: " + zdbID);
        Marker efg = markerRepository.getMarkerByID(zdbID);
        logger.info("gene: " + efg);
        markerBean.setMarker(efg);

        // not used, too much stuff excluded
//        MarkerService.createDefaultViewForMarker(markerBean);

        markerBean.setMarkerTypeDisplay(MarkerService.getMarkerTypeString(efg));

        markerBean.setPreviousNames(markerRepository.getPreviousNamesLight(efg));

        markerBean.setLatestUpdate(RepositoryFactory.getAuditLogRepository().getLatestAuditLogItem(zdbID));

        markerBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID)) ;

        // EXPRESSION SECTION
        markerBean.setMarkerExpression(expressionService.getExpressionForEfg(efg));

        // (CONSTRUCTS)
        Set<MarkerRelationship.Type> types = new HashSet<MarkerRelationship.Type>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION);
        markerBean.setConstructs(MarkerService.getRelatedMarker(efg, types));

        // (Antibodies)
        markerBean.setRelatedAntibodies(markerRepository
                .getRelatedMarkerDisplayForTypes(efg, true
                        , MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));


//      CITATIONS
        markerBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(efg.getZdbID()));

        model.addAttribute(LookupStrings.FORM_BEAN, markerBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.GENE.getTitleString() + efg.getAbbreviation());

        return "marker/efg-view.page";
    }
}