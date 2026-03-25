package org.zfin.sequence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

/**
 * Component class for STRSequence.
 * Only MO and CRISPR marker have sequence associated.
 * In case we decide to also include sequence info for general marker type we would make this a component
 * of the Marker class.
 */
@Embeddable
public class STRMarkerSequence {

    @Transient
    private String zdbID;
    @Transient
    private String name;

    @Column(name = "seq_sequence", nullable = false)
    private String sequence;

    @Column(name = "seq_offset_start")
    private Long offsetStart;

    @Column(name = "seq_offset_stop")
    private Long offsetStop;

    @Column(name = "seq_left_end", nullable = false)
    private String leftEnd;

    @Column(name = "seq_type")
    private String type;

    @Column(name = "seq_sequence_2")
    private String secondSequence;

    public String getSecondSequence() {
        return secondSequence;
    }

    public void setSecondSequence(String secondSequence) {
        this.secondSequence = secondSequence;
    }



    public String getType() {
        return "Nucleotide";
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
