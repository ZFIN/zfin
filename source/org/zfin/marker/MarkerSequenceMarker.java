package org.zfin.marker;

import org.zfin.sequence.MarkerSequence;

import java.util.Set;

/**
 * This represents Markers that have MarkerSequence's
 */
public class MarkerSequenceMarker extends Marker {

    private Set<MarkerSequence> sequences ;

    public Set<MarkerSequence> getSequences() {
        return sequences;
    }

    public void setSequences(Set<MarkerSequence> sequences) {
        this.sequences = sequences;
    }
}
