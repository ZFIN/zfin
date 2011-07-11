package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.service.AntibodyMarkerService;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

/**
 */
@Controller
public class AntibodyViewController {

    private Logger logger = Logger.getLogger(AntibodyViewController.class);


    @RequestMapping(value = "/antibody/view/{zdbID}")
    public String getAntibodyView(Model model
            , @PathVariable("zdbID") String zdbID
    ) throws Exception {
        // set base bean
        AntibodyMarkerBean antibodyBean = new AntibodyMarkerBean();

        logger.info("zdbID: " + zdbID);
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
        logger.info("antibody: " + antibody);
        antibodyBean.setMarker(antibody);


        // set standard stuff
        antibodyBean.setMarkerTypeDisplay(MarkerService.getMarkerTypeString(antibody));
        antibodyBean.setPreviousNames(RepositoryFactory.getMarkerRepository().getPreviousNamesLight(antibody));
        antibodyBean.setLatestUpdate(RepositoryFactory.getAuditLogRepository().getLatestAuditLogItem(zdbID));
        antibodyBean.setHasMarkerHistory(RepositoryFactory.getMarkerRepository().getHasMarkerHistory(zdbID)) ;

        // set other antibody data
        antibodyBean.setDistinctAssayNames(AntibodyMarkerService.getDistinctAssayNames(antibody));
        antibodyBean.setAntigenGenes(RepositoryFactory.getMarkerRepository().getRelatedMarkerDisplayForTypes(antibody,false, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));

       // set external notes (same as orthology)
        antibodyBean.setExternalNotes(RepositoryFactory.getInfrastructureRepository().getExternalNotes(antibody.getZdbID()));

       // set labeling
        antibodyBean.setAntibodyDetailedLabelings(AntibodyMarkerService.getAntibodyDetailedLabelings(antibody));
        antibodyBean.setNumberOfDistinctComposedTerms(AntibodyMarkerService.getNumberOfDistinctComposedTerms(antibody));

       // set source
        antibodyBean.setSuppliers(RepositoryFactory.getMarkerRepository().getSuppliersForMarker(antibody.getZdbID()));

//      CITATIONS
        antibodyBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberDirectPublications(antibody.getZdbID()));

        model.addAttribute(LookupStrings.FORM_BEAN, antibodyBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.ANTIBODY.getTitleString() + antibody.getAbbreviation());

        return "marker/antibody-view.page";
    }
}