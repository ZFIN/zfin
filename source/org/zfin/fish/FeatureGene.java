package org.zfin.fish;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.Marker;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.presentation.Construct;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FeatureGene {

    private long ID;
    private Feature feature;
    private Marker sequenceTargetingReagent;
    private Marker gene;
    private Marker construct;
    private String type;
    private String typeDisplay;
    private MutationType mutationTypeDisplay;


    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Marker getSequenceTargetingReagent() {
        return sequenceTargetingReagent;
    }

    public void setSequenceTargetingReagent(Marker sequenceTargetingReagent) {
        this.sequenceTargetingReagent = sequenceTargetingReagent;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Marker getConstruct() {
        return construct;
    }

    public void setConstruct(Marker construct) {
        this.construct = construct;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeDisplay() {
        return typeDisplay;
    }

    public void setTypeDisplay(String typeDisplay) {
        this.typeDisplay = typeDisplay;
    }

    public MutationType getMutationTypeDisplay() {
        if (mutationTypeDisplay != null)
            return mutationTypeDisplay;
        if (typeDisplay == null)
            return MutationType.UNKNOWN;
        return MutationType.getMutationType(typeDisplay);
    }
}
