package org.zfin.fish;

import org.zfin.infrastructure.ZfinEntity;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FeatureGene {

    private long ID;
    private ZfinEntity feature;
    private ZfinEntity gene;
    private ZfinEntity construct;
    private String type;
    private String typeDisplay;
    private MutationType mutationTypeDisplay;
    private FishAnnotation fishAnnotation;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public ZfinEntity getFeature() {
        return feature;
    }

    public void setFeature(ZfinEntity feature) {
        this.feature = feature;
    }

    public ZfinEntity getGene() {
        return gene;
    }

    public void setGene(ZfinEntity gene) {
        this.gene = gene;
    }

    public ZfinEntity getConstruct() {
        return construct;
    }

    public void setConstruct(ZfinEntity construct) {
        this.construct = construct;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FishAnnotation getFishAnnotation() {
        return fishAnnotation;
    }

    public void setFishAnnotation(FishAnnotation fishAnnotation) {
        this.fishAnnotation = fishAnnotation;
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
