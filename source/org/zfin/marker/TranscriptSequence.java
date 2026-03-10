package org.zfin.marker;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Component class for STRSequence.
 * Only MO and CRISPR marker have sequence associated.
 * In case we decide to also include sequence info for general marker type we would make this a component
 * of the Marker class.
 */
@Entity
@Table(name = "transcript_sequence")
public class TranscriptSequence {

    @Id
    @Column(name = "ts_transcript_zdb_id")
    private String zdbID;

    @Column(name = "ts_transcript_ottdart_id")
    private String ottdartid;

    @Column(name = "ts_sequence")
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
