package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelationshipDTO  {

    public static final String IS_MODEL_OF = "is_model_of";
    public static final String IS_MARKER_OF = "is_marker_of";
    public static final String FISH = "fish";
    public static final String GENE = "gene";

    private String associationType;
    private String objectType;

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
}
