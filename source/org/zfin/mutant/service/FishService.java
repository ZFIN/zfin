package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.CrossReference;
import org.zfin.alliancegenome.AgmRESTInterfaceAlliance;
import org.zfin.alliancegenome.AllianceRestManager;
import org.zfin.mutant.Fish;

import java.util.List;

@Log4j2
public class FishService {

    protected static final String ZEBRAFISH_TAXID = "taxon:7955";

    public static void submitFishToAlliance(Fish fish) {
        AffectedGenomicModel model = new AffectedGenomicModel();
        model.setCurie(fish.getZdbID());
        model.setName(fish.getDisplayName());
        model.setTaxon(ZEBRAFISH_TAXID);
        CrossReference reference = new CrossReference();
        reference.setCurie(fish.getZdbID());
        reference.setPageAreas(List.of("pages"));
        model.setCrossReferences(List.of(reference));

        AgmRESTInterfaceAlliance api = AllianceRestManager.getAgmEndpoints();
        try {
            api.addAffectedGenomicModel(model);
        } catch (Exception e) {
            log.error("Could not create Affected Genomic Model (Fish) at Alliance: " + e.getMessage());
        }
    }

    public static void main(String[] args){
        Fish fish = new Fish();
        fish.setZdbID("ZFIN:ZDB-FISH-220207-1");
        fish.setDisplayName("Fish Name");
        FishService.submitFishToAlliance(fish);
    }

}
