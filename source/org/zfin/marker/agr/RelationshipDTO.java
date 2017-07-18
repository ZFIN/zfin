package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelationshipDTO {

    public static final String IS_MODEL_OF = "is_model_of";
    public static final String CONTRIBUTES_TO_CONDITION = "contributes_to_condition";
    public static final String IS_MARKER_OF = "is_marker_of";
    public static final String FISH = "fish";
    public static final String ALELLE = "allele";
    public static final String GENE = "gene";

    private String associationType;
    private String objectType;
    private Set<String> inferredGeneAssociation;


    public RelationshipDTO(String associationType, String objectType) {
        this.associationType = associationType;
        this.objectType = objectType;
    }

    public String getAssociationType() {
        return associationType;
    }

    public String getObjectType() {
        return objectType;
    }

    public Set<String> getInferredGeneAssociation() {
        return inferredGeneAssociation;
    }

    public void setInferredGeneAssociation(Set<String> inferredGeneAssociation) {
        this.inferredGeneAssociation = inferredGeneAssociation;
    }
}
