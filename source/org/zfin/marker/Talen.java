package org.zfin.marker;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.sequence.TalenMarkerSequence;
import org.zfin.mutant.SequenceTargetingReagent;

public class Talen extends SequenceTargetingReagent {

    private TalenMarkerSequence sequenceExtension;

    private static Logger logger = LogManager.getLogger(Talen.class);

    public TalenMarkerSequence getSequenceExtension() {
        return sequenceExtension;
    }

    public void setSequenceExtension(TalenMarkerSequence sequenceExtension) {
        this.sequenceExtension = sequenceExtension;
    }





}
