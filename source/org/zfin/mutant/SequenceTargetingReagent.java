package org.zfin.mutant;

import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.sequence.MarkerSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Business entity for Morpholinos, TALEN and CRISPR.
 */
public class SequenceTargetingReagent extends Marker {

    private List<Marker> targetGenes;
    private MarkerSequence sequence;
    private static Logger logger = Logger.getLogger(SequenceTargetingReagent.class);

    /**
     * The target genes are calculated from the relationship property.
     * we cannot map the target genes directly through Hibernate.
     *
     * @return list of genes.
     */
    public List<Marker> getTargetGenes() {
        if (targetGenes != null)
            return targetGenes;
        targetGenes = new ArrayList<Marker>(1);
        for (MarkerRelationship relationship : firstMarkerRelationships) {
            targetGenes.add(relationship.getSecondMarker());
        }
        return targetGenes;
    }

    public void setTargetGenes(List<Marker> targetGenes) {
        this.targetGenes = targetGenes;
    }

    public MarkerSequence getSequence() {
        return sequence;
    }

    public void setSequence(MarkerSequence sequence) {
        this.sequence = sequence;
    }
}
