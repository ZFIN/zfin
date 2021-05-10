package org.zfin.marker.agr;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpressionStageIdentifiersDTO {

    private String stageName;
    private String stageTermId;
    private UberonSlimTermDTO stageUberonSlimTerm;

    public ExpressionStageIdentifiersDTO(String stageName, String stageTermId, UberonSlimTermDTO stageUberonSlimTerm) {
        this.stageName = stageName;
        this.stageTermId = stageTermId;
        this.stageUberonSlimTerm = stageUberonSlimTerm;
    }

}
