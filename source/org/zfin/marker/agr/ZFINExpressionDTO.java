package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZFINExpressionDTO extends BasicExpressionDTO {

    private String assayName;
    private List<CrossReferenceDTO> ensemblCrossReferences;

    public List<CrossReferenceDTO> getEnsemblCrossReferences() {
        return ensemblCrossReferences;
    }

    public void setEnsemblCrossReferences(List<CrossReferenceDTO> ensemblCrossReferences) {
        this.ensemblCrossReferences = ensemblCrossReferences;
    }


    public String getAssayName() {
        return assayName;
    }

    public void setAssayName(String assayName) {
        this.assayName = assayName;
    }



}
