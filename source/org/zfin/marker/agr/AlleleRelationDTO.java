package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlleleRelationDTO {

    private ObjectRelationDTO objectRelation;

    public ObjectRelationDTO getObjectRelation() {
        return objectRelation;
    }

    public void setObjectRelation(ObjectRelationDTO objectRelation) {
        this.objectRelation = objectRelation;
    }
}