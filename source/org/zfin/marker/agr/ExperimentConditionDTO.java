package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String NCBITaxonId;
    private String ChemicalOntologyId;

}
