
package org.zfin.sequence;

public class SNPMarkerSequence {
    private String targetSequence;
    private Long offsetStart;
    private Long offsetStop;
    private String variation;
    private String leftEnd;
    private String type;

    public void setType(String type) {
        this.type = type;
    }




    public String getType() {
        return "Nucleotide";
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }

    public String getStartToOffset() {
        return targetSequence.substring(0, offsetStart.intValue() - 1);
    }

    public String getAmbiguity() {
        return targetSequence.substring(offsetStart.intValue() - 1, offsetStart.intValue());
    }

    public String getOffsetToEnd() {
        return targetSequence.substring(offsetStart.intValue());
    }

    public String getTargetSequence() {
        return targetSequence;
    }

    public void setTargetSequence(String sequence) {
        this.targetSequence = sequence;
    }
    public String getSequence() {
        return targetSequence;
    }
    public void setSequence(String sequence) {
        this.targetSequence = sequence;
    }

    public String getLeftEnd() {
        return "5'";
    }

    public void setLeftEnd(String leftEnd) {
        this.leftEnd = leftEnd;
    }

    public Long getOffsetStart() {
        return offsetStart;
    }

    public void setOffsetStart(Long offsetStart) {
        this.offsetStart = offsetStart;
    }

    public Long getOffsetStop() {
        return offsetStop;
    }

    public void setOffsetStop(Long offsetStop) {
        this.offsetStop = offsetStop;
    }

}

