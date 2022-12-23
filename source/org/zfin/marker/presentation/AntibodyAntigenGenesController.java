package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.antibody.AntibodyAntigenGeneService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.marker.MarkerRelationship;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api")
public class AntibodyAntigenGenesController {


    @Autowired
    private AntibodyAntigenGeneService antibodyAntigenGeneService;

    @JsonView(View.AntibodyMarkerRelationshipAPI.class)
    @RequestMapping(value = "/antibody/{antibodyZdbId}/antigen-genes", method = RequestMethod.GET)
    public List<MarkerRelationshipPresentation> getAntigensForAntibody(@PathVariable String antibodyZdbId) {
        return antibodyAntigenGeneService.getAntigenGenes(antibodyZdbId);
    }


    @JsonView(View.AntibodyMarkerRelationshipAPI.class)
    @RequestMapping(value = "/antibody/{antibodyZdbId}/antigen-genes", method = RequestMethod.POST)
    public MarkerRelationshipPresentation addAntigenForAntibody(@PathVariable String antibodyZdbId,
                                                                       @RequestBody MarkerRelationshipPresentation formData) {

        HibernateUtil.createTransaction();
        MarkerRelationship relationship = antibodyAntigenGeneService.addAntigenGeneForAntibody(antibodyZdbId, formData.getAbbreviation(), formData.getAttributionZdbIDs());
        HibernateUtil.flushAndCommitCurrentSession();

        formData.setMarkerRelationshipZdbId(relationship.getZdbID());
        formData.setZdbID(relationship.getFirstMarker().getZdbID());
        formData.setNumberOfPublications(formData.getAttributionZdbIDs().size());

        return formData;
    }

    @RequestMapping(value = "/antibody/{antibodyZdbId}/antigen-genes/{mrelZdbId}", method = RequestMethod.DELETE)
    public void deleteAntigenForAntibody(@PathVariable String antibodyZdbId,
                                                                @PathVariable String mrelZdbId) throws Exception {
        HibernateUtil.createTransaction();
        antibodyAntigenGeneService.deleteAntigenGeneForAntibody(antibodyZdbId, mrelZdbId);
        HibernateUtil.flushAndCommitCurrentSession();
    }


    @JsonView(View.AntibodyMarkerRelationshipAPI.class)
    @RequestMapping(value = "/antibody/{antibodyZdbId}/antigen-genes/{mrelZdbId}", method = RequestMethod.POST)
    public MarkerRelationshipPresentation updateAntigenForAntibody(@PathVariable String antibodyZdbId,
                                                                   @PathVariable String mrelZdbId,
                                                                   @RequestBody MarkerRelationshipPresentation formData) {

        HibernateUtil.createTransaction();
        MarkerRelationship relationship = antibodyAntigenGeneService.updateAntigenGeneForAntibody(mrelZdbId, formData.getAbbreviation(), formData.getAttributionZdbIDs());
        HibernateUtil.flushAndCommitCurrentSession();

        formData.setMarkerRelationshipZdbId(relationship.getZdbID());
        formData.setZdbID(relationship.getFirstMarker().getZdbID());
        formData.setNumberOfPublications(formData.getAttributionZdbIDs().size());

        return formData;
    }

}
