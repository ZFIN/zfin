package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ExperimentConditionDTO  {

    private String conditionStatement;
    private String conditionClassId;
    private String conditionId;
    private String anatomicalOntologyId;
    private String geneOntologyId;
    @JsonProperty("NCBITaxonId")
    private String ncbiTaxonId;
//    @JsonProperty("ChemicalOntologyId")
    @JsonIgnore
    private String chemicalOntologyId;

}
