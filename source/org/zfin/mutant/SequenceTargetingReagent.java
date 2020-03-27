package org.zfin.mutant;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.sequence.STRMarkerSequence;
import org.zfin.sequence.TalenMarkerSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Business entity for Morpholinos, TALEN and CRISPR.
 */
public class SequenceTargetingReagent extends Marker {

    private List<Marker> targetGenes;
    private STRMarkerSequence sequence;
    private static Logger logger = LogManager.getLogger(SequenceTargetingReagent.class);

    /**
     * The target genes are calculated from the relationship property.
     * we cannot map the target genes directly through Hibernate.
     *
     * @return list of genes.
     */
    public List<Marker> getTargetGenes() {
        if (targetGenes != null)
            return targetGenes;
        targetGenes = new ArrayList<>(1);
        for (MarkerRelationship relationship : firstMarkerRelationships) {
            targetGenes.add(relationship.getSecondMarker());
        }
        return targetGenes;
    }

    public void setTargetGenes(List<Marker> targetGenes) {
        this.targetGenes = targetGenes;
    }

    public STRMarkerSequence getSequence() {
        return sequence;
    }

    public void setSequence(STRMarkerSequence sequence) {
        this.sequence = sequence;
    }
}
