package org.zfin.marker;

/**
 * Component class for STRSequence.
 * Only MO and CRISPR marker have sequence associated.
 * In case we decide to also include sequence info for general marker type we would make this a component
 * of the Marker class.
 */
public class TranscriptSequence {

    private String zdbID;
    private String ottdartid;
    private String sequence;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getOttdartid() {
        return ottdartid;
    }

    public void setOttdartid(String ottdartid) {
        this.ottdartid = ottdartid;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
