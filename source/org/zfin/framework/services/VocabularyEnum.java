package org.zfin.framework.services;

public enum VocabularyEnum {
    TRANSCRIPT_ANNOTATION_METHOD("transcript annotation method"),
    PREDICATE("predicate"),
    MAPPING_JUSTIFICATION("mapping justification"),
    INFERENCE_METHOD_CHEBI_MESH("inference method chebi-mesh");

    private String name;

    VocabularyEnum(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
