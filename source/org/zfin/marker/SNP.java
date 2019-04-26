package org.zfin.marker;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.sequence.SNPMarkerSequence;


public class SNP extends Marker {
    private SNPMarkerSequence sequence;
    private static Logger logger = LogManager.getLogger(SNP.class);

    public SNPMarkerSequence getSequence() {
        return sequence;
    }

    public void setSequence(SNPMarkerSequence sequence) {
        this.sequence = sequence;
    }


}
