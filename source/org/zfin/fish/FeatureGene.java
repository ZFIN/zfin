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
    private String parentalZygosityDisplay;

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

    public String getParentalZygosityDisplay() {
        return parentalZygosityDisplay;
    }

    public void setParentalZygosityDisplay(String parentalZygosityDisplay) {
        this.parentalZygosityDisplay = parentalZygosityDisplay;
    }

    public MutationType getMutationTypeDisplay() {
        if (mutationTypeDisplay != null) {
            return mutationTypeDisplay;
        }
        if (typeDisplay == null) {
            return MutationType.UNKNOWN;
        }
        return MutationType.getMutationType(typeDisplay);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureGene that = (FeatureGene) o;

        if (!feature.equals(that.feature)) return false;
        if (sequenceTargetingReagent != null ? !sequenceTargetingReagent.equals(that.sequenceTargetingReagent) : that.sequenceTargetingReagent != null) {
            return false;
        }
        if (gene != null ? !gene.equals(that.gene) : that.gene != null) return false;
        return !(construct != null ? !construct.equals(that.construct) : that.construct != null);

    }

    @Override
    public int hashCode() {
        int result = feature.hashCode();
        result = 31 * result + (sequenceTargetingReagent != null ? sequenceTargetingReagent.hashCode() : 0);
        result = 31 * result + (gene != null ? gene.hashCode() : 0);
        result = 31 * result + (construct != null ? construct.hashCode() : 0);
        return result;
    }
}
