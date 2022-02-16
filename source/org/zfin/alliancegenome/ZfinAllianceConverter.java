package org.zfin.alliancegenome;

import org.alliancegenome.curation_api.model.entities.*;
import org.alliancegenome.curation_api.model.entities.ontology.DOTerm;
import org.alliancegenome.curation_api.model.entities.ontology.EcoTerm;
import org.alliancegenome.curation_api.model.entities.ontology.NCBITaxonTerm;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.List;

import static org.zfin.mutant.service.FishService.ZEBRAFISH_TAXID;

public class ZfinAllianceConverter {


    public static DiseaseAnnotation convertDiseaseAnnotation(DiseaseAnnotationModel model) {
        AGMDiseaseAnnotation annotation = new AGMDiseaseAnnotation();
        annotation.setObject(convertDisease(model.getDiseaseAnnotation().getDisease(), model.getDiseaseAnnotation().getPublication()));
        annotation.setSubject(convertFish(model.getFishExperiment().getFish()));
        annotation.setEvidenceCodes(convertEvidenceCodes(model.getDiseaseAnnotation().getEvidenceCode()));
        //annotation.setReferenceList(convertReferences(model.getDiseaseAnnotation().getPublication()));
        annotation.setDiseaseRelation(DiseaseAnnotation.DiseaseRelation.is_model_of);
        annotation.setModId(model.getDiseaseAnnotation().getZdbID());
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

    private static AffectedGenomicModel convertFish(Fish fish) {
        AffectedGenomicModel entity = new AffectedGenomicModel();
        entity.setCurie("ZFIN:" + fish.getZdbID());
        NCBITaxonTerm term = new NCBITaxonTerm();
        term.setName(ZEBRAFISH_TAXID);
        entity.setTaxon(term);
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
