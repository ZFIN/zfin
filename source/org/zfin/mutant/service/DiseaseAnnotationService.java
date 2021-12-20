package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.BiologicalEntity;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.Reference;
import org.alliancegenome.curation_api.model.entities.ontology.DOTerm;
import org.alliancegenome.curation_api.model.entities.ontology.EcoTerm;
import org.zfin.alliancegenome.AllianceRestManager;
import org.zfin.alliancegenome.DiseaseAnnotationRESTInterfaceAlliance;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;

import java.util.List;

@Log4j2
public class DiseaseAnnotationService {

    protected static final String ZEBRAFISH_TAXID = "taxon:7955";

    public static void submitAnnotationToAlliance(DiseaseAnnotationModel dam) {
        DiseaseAnnotation da = new DiseaseAnnotation();
        da.setObject(getDoTerm(dam.getDiseaseAnnotation().getDisease()));
        da.setSubject(getBiologicalEntity(dam.getFishExperiment().getFish()));
        da.setDiseaseRelation(DiseaseAnnotation.DiseaseRelation.is_model_of);
        da.setNegated(Boolean.FALSE);
        da.setEvidenceCodes(List.of(getEvidenceCodes(dam.getDiseaseAnnotation())));
        da.setReferenceList(List.of(getCrossReference(dam.getDiseaseAnnotation())));
        DiseaseAnnotationRESTInterfaceAlliance api = AllianceRestManager.getDiseaseAnnotationEndpoints();
        try {
            api.addDiseaseAnnotation(da);
        } catch (Exception e) {
            log.error("Could not create Disease Annotation at Alliance: " + e.getMessage());
        }
    }

    private static Reference getCrossReference(org.zfin.mutant.DiseaseAnnotation diseaseAnnotation) {
        Reference reference = new Reference();
        reference.setCurie("PMID:" + diseaseAnnotation.getPublication().getAccessionNumber());
        return reference;
    }

    private static EcoTerm getEvidenceCodes(org.zfin.mutant.DiseaseAnnotation dam) {
        EcoTerm evidence = new EcoTerm();
        if (dam.getEvidenceCode().equals("ZDB-TERM-170419-250"))
            evidence.setCurie("ECO:0000304");
        if (dam.getEvidenceCode().equals("ZDB-TERM-170419-251"))
            evidence.setCurie("ECO:0000305");
        return evidence;
    }

    private static BiologicalEntity getBiologicalEntity(Fish fish) {
        BiologicalEntity entity = new BiologicalEntity();
        entity.setCurie("ZFIN:" + fish.getZdbID());
        entity.setTaxon(ZEBRAFISH_TAXID);
        return entity;
    }

    private static DOTerm getDoTerm(GenericTerm disease) {
        DOTerm term = new DOTerm();
        term.setCurie(disease.getOboID());
        return term;
    }
}
