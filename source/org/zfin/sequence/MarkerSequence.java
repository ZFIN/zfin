package org.zfin.sequence;

/**
 * Component class for SequenceTargetingReagent.
 * Only MO, TALEN and CRISPR marker have sequence associated.
 * In case we decide to also include sequence info for general marker type we would make this a component
 * of the Marker class.
 */
public class MarkerSequence {

    private String sequence;
    private String secondSequence;
    private Long offsetStart;
    private Long offsetStop;
    private String variation;
    private String type;

    public String getStartToOffset() {
        return sequence.substring(0, offsetStart.intValue() - 1);
    }

    public String getAmbiguity() {
        return sequence.substring(offsetStart.intValue() - 1, offsetStart.intValue());
    }

    public String getOffsetToEnd() {
        return sequence.substring(offsetStart.intValue());
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getLeftEnd() {
        return "5'";
    }

    /*
    public String getType(){
        return "Genomic" ;
    }  */

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

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }

    public String getSecondSequence() {
        return secondSequence;
    }

    public void setSecondSequence(String secondSequence) {
        this.secondSequence = secondSequence;
    }

    public String getType() {
        if (type != null) {
            return type;
        } else {
            return "Genomic";
        }
    }

    public void setType(String sequenceType) {
        this.type = sequenceType;
    }
}
