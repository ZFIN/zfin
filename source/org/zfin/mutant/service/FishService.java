package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.CrossReference;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.zfin.alliancegenome.AgmRESTInterfaceAlliance;
import org.zfin.alliancegenome.AllianceRestManager;
import org.zfin.mutant.Fish;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.List;

@Log4j2
public class FishService extends AllianceService {

    public static void submitFishToAlliance(Fish fish) {
        AffectedGenomicModel model = new AffectedGenomicModel();
        model.setCurie("ZFIN:" + fish.getZdbID());
        model.setName(fish.getDisplayName());
        model.setTaxon(getNcbiTaxonTerm());
        CrossReference reference = new CrossReference();
        reference.setCurie(model.getCurie());
        reference.setPageAreas(List.of("pages"));
//        model.setCrossReferences(List.of(reference));

        AgmRESTInterfaceAlliance api = AllianceRestManager.getAgmEndpoints();
        ObjectResponse<AffectedGenomicModel> agmFish = null;
        try {
            agmFish = api.addAffectedGenomicModel(model);
        } catch (Exception e) {
            log.error("Could not create Affected Genomic Model (Fish) at Alliance: " + e.getMessage());
        }
        log.info("Done loading");
    }

    public static void main(String[] args) {
        ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("http://localhost:8080");
        Fish fish = new Fish();
        fish.setZdbID("ZFIN:ZDB-FISH-220207-1");
        fish.setDisplayName("Fish Name");
        FishService.submitFishToAlliance(fish);
    }

}
