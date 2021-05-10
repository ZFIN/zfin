package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.mutant.SequenceTargetingReagent;

public class STRTargetRow {

    private Marker target;
    private SequenceTargetingReagent str;

    public STRTargetRow(SequenceTargetingReagent str, Marker target) {
        this.target = target;
        this.str = str;
    }

    public Marker getTarget() {
        return target;
    }

    public void setTarget(Marker target) {
        this.target = target;
    }

    public SequenceTargetingReagent getStr() {
        return str;
    }

    public void setStr(SequenceTargetingReagent str) {
        this.str = str;
    }
}
