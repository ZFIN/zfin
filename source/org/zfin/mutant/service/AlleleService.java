package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.Allele;
import org.alliancegenome.curation_api.model.entities.VocabularyTerm;
import org.alliancegenome.curation_api.model.entities.slotAnnotations.AlleleFullNameSlotAnnotation;
import org.alliancegenome.curation_api.model.entities.slotAnnotations.AlleleSymbolSlotAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.springframework.stereotype.Service;
import org.zfin.alliancegenome.AlleleRESTAllianceService;
import org.zfin.feature.Feature;
import org.zfin.properties.ZfinPropertiesEnum;

@Log4j2
@Service
public class AlleleService extends AllianceService {

    AlleleRESTAllianceService restInterfaceAlliance = new AlleleRESTAllianceService();

    public void submitAlleleToAlliance(Feature feature) {
        Allele allele = new Allele();
        allele.setPrimaryExternalId("ZFIN:" + feature.getZdbID());
        AlleleSymbolSlotAnnotation symbol = new AlleleSymbolSlotAnnotation();
        symbol.setDisplayText(feature.getAbbreviation());
        symbol.setFormatText(feature.getAbbreviation());
        VocabularyTerm symbolName = new VocabularyTerm();
        symbolName.setName("nomenclature_symbol");
        symbol.setNameType(symbolName);
        allele.setAlleleSymbol(symbol);

        AlleleFullNameSlotAnnotation nameDtoName = new AlleleFullNameSlotAnnotation();
        nameDtoName.setDisplayText(feature.getName());
        nameDtoName.setFormatText(feature.getName());
        VocabularyTerm fullName = new VocabularyTerm();
        fullName.setName("full_name");
        nameDtoName.setNameType(fullName);
        allele.setAlleleFullName(nameDtoName);

        allele.setTaxon(getNcbiTaxonTerm());

        ObjectResponse<Allele> alleleResponse = null;
        try {
            alleleResponse = restInterfaceAlliance.addAffectedGenomicModel(allele);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not create Allele at Alliance: " + message);
            log.debug("Could not create Allele at Alliance: ", e);
        }
        log.info("Done loading");
    }

    public static void main(String[] args) {
        //ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("http://localhost:8080");
        ZfinPropertiesEnum.TARGETROOT.setValue(".");
        ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("https://alpha-curation.alliancegenome.org");
        Feature feature = new Feature();
        feature.setZdbID("v");
        feature.setAbbreviation("walter von der Vogelheide");
        feature.setName("walter von der Vogelheide");
        AlleleService service = new AlleleService();
        service.restInterfaceAlliance = new AlleleRESTAllianceService();
        service.submitAlleleToAlliance(feature);
    }
}
