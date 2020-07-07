package org.zfin.marker.agr;


import lombok.Getter;
import lombok.Setter;
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
