package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ConditionRelationDTO {

    private String conditionRelationType;
    private List<ExperimentConditionDTO> conditions;

}
