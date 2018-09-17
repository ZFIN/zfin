package org.zfin.marker.agr;

public class ExpressionStageIdentifiersDTO {

    private String stageName;
    private String stageTermId;
    private UberonSlimTermDTO stageUberonSlimTerm;

    public ExpressionStageIdentifiersDTO(String stageName, String stageTermId, UberonSlimTermDTO stageUberonSlimTerm) {
        this.stageName = stageName;
        this.stageTermId = stageTermId;
        this.stageUberonSlimTerm = stageUberonSlimTerm;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getStageTermId() {
        return stageTermId;
    }

    public void setStageTermId(String stageTermId) {
        this.stageTermId = stageTermId;
    }

    public UberonSlimTermDTO getStageUberonSlimTerm() {
        return stageUberonSlimTerm;
    }

    public void setStageUberonSlimTerm(UberonSlimTermDTO stageUberonSlimTerm) {
        this.stageUberonSlimTerm = stageUberonSlimTerm;
    }
}
