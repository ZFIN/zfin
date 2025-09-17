package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.*;
import org.alliancegenome.curation_api.model.entities.slotAnnotations.AgmFullNameSlotAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.alliancegenome.AgmRESTAllianceService;
import org.zfin.mutant.Fish;
import org.zfin.properties.ZfinPropertiesEnum;

@Log4j2
@Service
public class FishService extends AllianceService {

    @Autowired
    AgmRESTAllianceService agmRESTAllianceService = new AgmRESTAllianceService();

    public void submitFishToAlliance(Fish fish) {
        AffectedGenomicModel model = new AffectedGenomicModel();
        model.setPrimaryExternalId("ZFIN:" + fish.getZdbID());
        AgmFullNameSlotAnnotation agmFullName = new AgmFullNameSlotAnnotation();
        agmFullName.setDisplayText(fish.getDisplayName());
        agmFullName.setFormatText(fish.getDisplayName());
        VocabularyTerm nameType = new VocabularyTerm();
        nameType.setName("full_name");
        agmFullName.setNameType(nameType);
        model.setAgmFullName(agmFullName);
        VocabularyTerm term = new VocabularyTerm();
        term.setName("fish");
        model.setSubtype(term);
        model.setTaxon(getNcbiTaxonTerm());
        CrossReference reference = new CrossReference();
        reference.setReferencedCurie(model.getCurie());
        ResourceDescriptorPage page = new ResourceDescriptorPage();
        page.setPageDescription("pages");
        ResourceDescriptor resourceDescriptor = new ResourceDescriptor();
        resourceDescriptor.setPrefix("ZFIN");
        page.setResourceDescriptor(resourceDescriptor);
        reference.setResourceDescriptorPage(page);
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
        //ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("http://localhost:8080");
        ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("https://alpha-curation.alliancegenome.org");
        ZfinPropertiesEnum.TARGETROOT.setValue(".");
        Fish fish = new Fish();
        fish.setZdbID("ZDB-FISH-220707-340");
        fish.setDisplayName("Fish Name");
        FishService service = new FishService();
        service.agmRESTAllianceService = new AgmRESTAllianceService();
        service.submitFishToAlliance(fish);
    }

}
