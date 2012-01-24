package org.zfin.mutant;

import org.geneontology.oboedit.datamodel.Relationship;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerSequenceMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * ToDo: Please add documentation for this class.
 */
public class Morpholino extends MarkerSequenceMarker {

    private List<Marker> targetGenes;

    /**
     * The target genes are calculated from the relationship property.
     * we cannot map the target genes directly through Hibernate.
     * @return list of genes.
     */
    public List<Marker> getTargetGenes() {
        if(targetGenes != null)
            return targetGenes;
        targetGenes = new ArrayList<Marker>(1);
        for(MarkerRelationship relationship: firstMarkerRelationships){
            targetGenes.add(relationship.getSecondMarker());
        }
        return targetGenes;
    }

    public void setTargetGenes(List<Marker> targetGenes) {
        this.targetGenes = targetGenes;
    }
}
