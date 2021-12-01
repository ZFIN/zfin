package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.BiologicalEntity;
import org.alliancegenome.curation_api.model.entities.CrossReference;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.Reference;
import org.alliancegenome.curation_api.model.entities.ontology.DOTerm;
import org.alliancegenome.curation_api.model.entities.ontology.EcoTerm;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.List;

public class ZfinAllianceConverter {


    public static DiseaseAnnotation convertDiseaseAnnotation(DiseaseAnnotationModel model) {
        DiseaseAnnotation annotation = new DiseaseAnnotation();
        annotation.setObject(convertDisease(model.getDiseaseAnnotation().getDisease(), model.getDiseaseAnnotation().getPublication()));
        annotation.setSubject(convertFish(model.getFishExperiment().getFish()));
        annotation.setEvidenceCodes(convertEvidenceCodes(model.getDiseaseAnnotation().getEvidenceCode()));
        //annotation.setReferenceList(convertReferences(model.getDiseaseAnnotation().getPublication()));
        annotation.setDiseaseRelation(DiseaseAnnotation.DiseaseRelation.is_model_of);
        annotation.setCurie(model.getDiseaseAnnotation().getZdbID());
        return annotation;
    }

    private static List<Reference> convertReferences(Publication publication) {
        Reference reference = new Reference();
        reference.setCurie("PMID:16530747");
        return List.of(reference);
    }

    private static List<EcoTerm> convertEvidenceCodes(String evidenceCode) {
        EcoTerm eco = new EcoTerm();
        eco.setCurie("ECO:0001547");
        eco.setName(evidenceCode);
        return List.of(eco);
    }

    private static BiologicalEntity convertFish(Fish fish) {
        BiologicalEntity entity = new BiologicalEntity();
        entity.setCurie("ZFIN:" + fish.getZdbID());
        entity.setTaxon("NCBITaxon:7955");
        return entity;
    }

    private static DOTerm convertDisease(GenericTerm disease, Publication publication) {
        DOTerm doTerm = new DOTerm();
        doTerm.setCurie(disease.getOboID());
        CrossReference reference = new CrossReference();
        reference.setCurie("PU");
        doTerm.setCrossReferences(List.of(reference));
        return doTerm;
    }
}
