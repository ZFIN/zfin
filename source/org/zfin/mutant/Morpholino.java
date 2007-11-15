package org.zfin.mutant;

import org.zfin.marker.Marker;

/**
 * ToDo: Please add documentation for this class.
 */
public class Morpholino extends Marker {

    private Marker targetGene;

    public Marker getTargetGene() {
        return targetGene;
    }

    public void setTargetGene(Marker targetGene) {
        this.targetGene = targetGene;
    }
}
