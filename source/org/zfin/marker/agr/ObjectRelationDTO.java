package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectRelationDTO {

    private String associationType;
    private String gene;
    private String construct;

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getConstruct() {
        return construct;
    }

    public void setConstruct(String construct) {
        this.construct = construct;
    }

    public String getAssociationType() {
        return associationType;
    }
    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }
}
