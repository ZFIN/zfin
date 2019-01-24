package org.zfin.marker.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.ExternalNote;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.framework.presentation.Area;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/antibody")
@Log4j2
public class AntibodyViewController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private MarkerService markerService;

    @RequestMapping(value = "/view/{zdbID}")
    public String getNewAntibodyView(Model model, @PathVariable("zdbID") String zdbID) throws Exception {
        // set base bean
        AntibodyMarkerBean antibodyBean = new AntibodyMarkerBean();

        zdbID = markerService.getActiveMarkerID(zdbID);
        log.info("zdbID: " + zdbID);
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
        log.info("antibody: " + antibody);
        antibodyBean.setMarker(antibody);


        // set standard stuff
        antibodyBean.setMarkerTypeDisplay(MarkerService.getMarkerTypeString(antibody));
        antibodyBean.setPreviousNames(markerRepository.getPreviousNamesLight(antibody));
        antibodyBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID));

        // set other antibody data
        antibodyBean.setAntigenGenes(markerRepository.getRelatedMarkerDisplayForTypes(antibody, false, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY));

        // set external notes (same as orthology)
        List<ExternalNote> listOfNotes = new ArrayList<>(antibody.getExternalNotes());
        antibodyBean.setExternalNotes(listOfNotes);

        // set labeling
        AntibodyService service = new AntibodyService(antibody);
        antibodyBean.setAntibodyDetailedLabelings(service.getAntibodyDetailedLabelings());
        antibodyBean.setNumberOfDistinctComposedTerms(service.getNumberOfDistinctComposedTerms());

        // set source
        antibodyBean.setSuppliers(markerRepository.getSuppliersForMarker(antibody.getZdbID()));

        antibodyBean.setAbRegistryID(markerRepository.getABRegID(antibody.getZdbID()));

//      CITATIONS
        antibodyBean.setNumPubs(RepositoryFactory.getPublicationRepository().getNumberDirectPublications(antibody.getZdbID()));

        model.addAttribute(LookupStrings.FORM_BEAN, antibodyBean);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, Area.ANTIBODY.getTitleString() + antibody.getName());

        return "marker/antibody/antibody-view";
    }

}