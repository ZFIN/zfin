package org.zfin.marker.agr;

import java.util.List;

public class ObjectRelationship {

    private String associationType;
    private String objectType;
    private List<String> inferredGeneAssociation;

    public ObjectRelationship(String associationType, String objectType) {
        this.associationType = associationType;
        this.objectType = objectType;
    }

    public String getAssociationType() {
        return associationType;
    }

    public String getObjectType() {
        return objectType;
    }

    public List<String> getInferredGeneAssociation() {
        return inferredGeneAssociation;
    }

    public void setInferredGeneAssociation(List<String> inferredGeneAssociation) {
        this.inferredGeneAssociation = inferredGeneAssociation;
    }
}
