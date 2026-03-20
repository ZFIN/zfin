package org.zfin.marker;

import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.sequence.SNPMarkerSequence;

@Entity
@Table(name = "snp_sequence")
@PrimaryKeyJoinColumn(name = "seq_mrkr_zdb_id")
public class SNP extends Marker {
    @Embedded
    private SNPMarkerSequence sequence;
    private static Logger logger = LogManager.getLogger(SNP.class);

    public SNPMarkerSequence getSequence() {
        return sequence;
    }

    public void setSequence(SNPMarkerSequence sequence) {
        this.sequence = sequence;
    }


}
