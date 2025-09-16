package org.zfin.marker.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.Gene;
import org.alliancegenome.curation_api.model.entities.VocabularyTerm;
import org.alliancegenome.curation_api.model.entities.ontology.SOTerm;
import org.alliancegenome.curation_api.model.entities.slotAnnotations.GeneFullNameSlotAnnotation;
import org.alliancegenome.curation_api.model.entities.slotAnnotations.GeneSymbolSlotAnnotation;
import org.alliancegenome.curation_api.response.ObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.alliancegenome.GeneRESTAllianceService;
import org.zfin.marker.Marker;
import org.zfin.mutant.service.AllianceService;
import org.zfin.properties.ZfinPropertiesEnum;

@Log4j2
@Service
public class GeneAllianceService extends AllianceService {

    @Autowired
    GeneRESTAllianceService restAllianceService = new GeneRESTAllianceService();

    public void submitGeneToAlliance(Marker marker) {
        Gene model = new Gene();
        model.setPrimaryExternalId("ZFIN:" + marker.getZdbID());
        GeneFullNameSlotAnnotation nameDtoName = new GeneFullNameSlotAnnotation();
        nameDtoName.setDisplayText(marker.getName());
        nameDtoName.setFormatText(marker.getName());
        VocabularyTerm fullName = new VocabularyTerm();
        fullName.setName("full_name");
        nameDtoName.setNameType(fullName);
        model.setGeneFullName(nameDtoName);

        GeneSymbolSlotAnnotation nameDtoSymbol = new GeneSymbolSlotAnnotation();
        nameDtoSymbol.setDisplayText(marker.getName());
        nameDtoSymbol.setFormatText(marker.getName());
        VocabularyTerm term = new VocabularyTerm();
        term.setName("nomenclature_symbol");
        nameDtoSymbol.setNameType(term);
        model.setGeneSymbol(nameDtoSymbol);
        SOTerm soTerm = new SOTerm();
        soTerm.setName("protein_coding_gene");
        soTerm.setId(392916L);
        model.setGeneType(soTerm);
        model.setTaxon(getNcbiTaxonTerm());

        ObjectResponse<Gene> geneResponse = null;
        try {
            geneResponse = restAllianceService.addGene(model);
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : e.getCause().getLocalizedMessage();
            log.error("Could not create Gene at Alliance: " + message);
            log.debug("Could not create Gene at Alliance: ", e);
        }
        log.info("Done loading");
    }

    public static void main(String[] args) {
        ZfinPropertiesEnum.TARGETROOT.setValue(".");
        //ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("http://localhost:8080");
        ZfinPropertiesEnum.ALLIANCE_CURATION_URL.setValue("https://alpha-curation.alliancegenome.org");
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-220707-340");
        marker.setName("Harry's majesty");
        marker.setAbbreviation("harryminarry");
        GeneAllianceService service = new GeneAllianceService();
        service.restAllianceService = new GeneRESTAllianceService();
        service.submitGeneToAlliance(marker);
    }

}
