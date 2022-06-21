package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.CrossReference;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.alliancegenome.AgmRESTAllianceService;
import org.zfin.mutant.Fish;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.List;

@Log4j2
@Service
public class FishService extends AllianceService {

    @Autowired
    AgmRESTAllianceService agmRESTAllianceService;

    public void submitFishToAlliance(Fish fish) {
        AffectedGenomicModel model = new AffectedGenomicModel();
        model.setCurie("ZFIN:" + fish.getZdbID());
        model.setName(fish.getDisplayName());
        model.setTaxon(getNcbiTaxonTerm());
        CrossReference reference = new CrossReference();
        reference.setCurie(model.getCurie());
        reference.setPageAreas(List.of("pages"));
//        model.setCrossReferences(List.of(reference));

        ObjectResponse<AffectedGenomicModel> agmFish = null;
        try {
            agmFish = agmRESTAllianceService.addAffectedGenomicModel(model);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not create Affected Genomic Model (Fish) at Alliance: " + message);
            log.debug("Could not create Affected Genomic Model (Fish) at Alliance: ", e);
        }
        log.info("Done loading");
    }

    public static void main(String[] args) {
        ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("http://localhost:8080");
        Fish fish = new Fish();
        fish.setZdbID("ZFIN:ZDB-FISH-220207-33");
        fish.setDisplayName("Fish Name");
        FishService service = new FishService();
        service.agmRESTAllianceService = new AgmRESTAllianceService();
        service.submitFishToAlliance(fish);
    }

}
