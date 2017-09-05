package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExperimentalConditionDTO {

    private String textCondition;
    private String zecoId;
    private String anatomicalId;
    private String geneOntologyId;
    @JsonProperty("NCBITaxonId")
    private String ncbiTaxonIdId;
    @JsonProperty("ChebiOntologyId")
    private String chebiOntologyId;
    @JsonProperty("ConditionIsStandard")
    private boolean conditionIsStandard;

    public ExperimentalConditionDTO(String textCondition, String zecoId) {
        this.textCondition = textCondition;
        this.zecoId = zecoId;
    }

    public String getTextCondition() {
        return textCondition;
    }

    public String getZecoId() {
        return zecoId;
    }

    public String getAnatomicalId() {
        return anatomicalId;
    }

    public void setAnatomicalId(String anatomicalId) {
        this.anatomicalId = anatomicalId;
    }

    public String getGeneOntologyId() {
        return geneOntologyId;
    }

    public void setGeneOntologyId(String geneOntologyId) {
        this.geneOntologyId = geneOntologyId;
    }

    public String getNcbiTaxonIdId() {
        return ncbiTaxonIdId;
    }

    public void setNcbiTaxonIdId(String ncbiTaxonIdId) {
        this.ncbiTaxonIdId = ncbiTaxonIdId;
    }

    public String getChebiOntologyId() {
        return chebiOntologyId;
    }

    public void setChebiOntologyId(String chebiOntologyId) {
        this.chebiOntologyId = chebiOntologyId;
    }

    public boolean isConditionIsStandard() {
        return zecoId.equals("ZECO:0000103");
    }
}
