package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ExperimentConditionDTO extends ZfinDTO {

    private String conditionStatement;
    private String conditionClassId;
    private String conditionId;
    private String anatomicalOntologyId;
    private String geneOntologyId;
    private String NCBITaxonId;
    private String ChemicalOntologyId;
    private String zecoId;

    public String getZecoId() {
        return zecoId;
    }

    public void setZecoId(String zecoId) {
        this.zecoId = zecoId;
    }

    public ExperimentConditionDTO(String zecoId) {
        this.zecoId = zecoId;
    }

    public String getConditionStatement() {
        return conditionStatement;
    }

    public void setConditionStatement(String conditionStatement) {
        this.conditionStatement = conditionStatement;
    }

    public String getConditionClassId() {
        return conditionClassId;
    }

    public void setConditionClassId(String conditionClassId) {
        this.conditionClassId = conditionClassId;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getAnatomicalOntologyId() {
        return anatomicalOntologyId;
    }

    public void setAnatomicalOntologyId(String anatomicalOntologyId) {
        this.anatomicalOntologyId = anatomicalOntologyId;
    }

    public String getGeneOntologyId() {
        return geneOntologyId;
    }

    public void setGeneOntologyId(String geneOntologyId) {
        this.geneOntologyId = geneOntologyId;
    }

    public String getNCBITaxonId() {
        return NCBITaxonId;
    }

    public void setNCBITaxonId(String NCBITaxonId) {
        this.NCBITaxonId = NCBITaxonId;
    }

    public String getChemicalOntologyId() {
        return ChemicalOntologyId;
    }

    public void setChemicalOntologyId(String chemicalOntologyId) {
        ChemicalOntologyId = chemicalOntologyId;
    }


}
