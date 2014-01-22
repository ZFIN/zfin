package org.zfin.sequence;

public class SNPMarkerSequence {
    private String sequenceItself;
    private Long offsetStart;
    private Long offsetStop;
    private String variation;
    private String leftEnd;
    private String type;


    public String getType() {
        return "Nucleotide";
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }

    public String getStartToOffset() {
        return sequenceItself.substring(0, offsetStart.intValue() - 1);
    }

    public String getAmbiguity() {
        return sequenceItself.substring(offsetStart.intValue() - 1, offsetStart.intValue());
    }

    public String getOffsetToEnd() {
        return sequenceItself.substring(offsetStart.intValue());
    }

    public String getSequenceItself() {
        return sequenceItself;
    }

    public void setSequenceItself(String sequence) {
        this.sequenceItself = sequence;
    }
    public String getSequence() {
        return sequenceItself;
    }
    public void setSequence(String sequence) {
        this.sequenceItself = sequence;
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
