package org.zfin.marker.agr;
import org.zfin.marker.agr.ExperimentConditionDTO;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ConditionRelationDTO {

    private String conditionRelationType;
    private List<ExperimentConditionDTO> conditions;

    public String getConditionRelationType() {
        return conditionRelationType;
    }

    public void setConditionRelationType(String conditionRelationType) {
        this.conditionRelationType = conditionRelationType;
    }

    public List<ExperimentConditionDTO> getConditions() {
        return conditions;
    }

    public void setConditions(List<ExperimentConditionDTO> conditions) {
        this.conditions = conditions;
    }
}
