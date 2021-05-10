package org.zfin.fish;

import org.zfin.mutant.Genotype;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FishAnnotation {

    private long ID;
    private String genotypeID;
    private String genotypeExperimentIds;
    private String featureGroupName;
    private String sequenceTargetingReagentGroupName;
    private String name;
    private String uniqueName;
    private String phenotypeFigureGroupName;
    private int phenotypeFigureCount;
    private int expressionFigureCount;
    private boolean hasExpressionImages;
    //todo: probably remove these, they're around mostly to help develop the sorting algorithm
    private String geneOrFeatureText;
    private String scoringText;

    public String getFishID() {
        String fishID = getGenotypeID();
        if (getGenotypeExperimentIds() != null)
            return fishID + "," + getGenotypeExperimentIds();
        return fishID;
    }

    public String getGenotypeExperimentIds() {
        return genotypeExperimentIds;
    }

    public void setGenotypeExperimentIds(String genotypeExperimentIds) {
        this.genotypeExperimentIds = genotypeExperimentIds;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getGenotypeID() {
        return genotypeID;
    }

    public void setGenotypeID(String genotypeID) {
        this.genotypeID = genotypeID;
    }

    public String getFeatureGroupName() {
        return featureGroupName;
    }

    public void setFeatureGroupName(String featureGroupName) {
        this.featureGroupName = featureGroupName;
    }

    public String getSequenceTargetingReagentGroupName() {
        return sequenceTargetingReagentGroupName;
    }

    public void setSequenceTargetingReagentGroupName(String sequenceTargetingReagentGroupName) {
        this.sequenceTargetingReagentGroupName = sequenceTargetingReagentGroupName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPhenotypeFigureCount() {
        return phenotypeFigureCount;
    }

    public void setPhenotypeFigureCount(int phenotypeFigureCount) {
        this.phenotypeFigureCount = phenotypeFigureCount;
    }

    public String getPhenotypeFigureGroupName() {
        return phenotypeFigureGroupName;
    }

    public void setPhenotypeFigureGroupName(String phenotypeFigureGroupName) {
        this.phenotypeFigureGroupName = phenotypeFigureGroupName;
    }

    public String getGeneOrFeatureText() {
        return geneOrFeatureText;
    }

    public void setGeneOrFeatureText(String geneOrFeatureText) {
        this.geneOrFeatureText = geneOrFeatureText;
    }

    public String getScoringText() {
        return scoringText;
    }

    public void setScoringText(String scoringText) {
        this.scoringText = scoringText;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public int getExpressionFigureCount() {
        return expressionFigureCount;
    }

    public void setExpressionFigureCount(int expressionFigureCount) {
        this.expressionFigureCount = expressionFigureCount;
    }

    public boolean isHasExpressionImages() {
        return hasExpressionImages;
    }

    public void setHasExpressionImages(boolean hasExpressionImages) {
        this.hasExpressionImages = hasExpressionImages;
    }

    public boolean equals(Object other) {
        if (!(other instanceof FishAnnotation)) {
            if (other instanceof Genotype) {
                return ((Genotype) other).getZdbID().equals(getGenotypeID());
            } else {
                return false;
            }
        }
        FishAnnotation anotherFishAnnotation = (FishAnnotation) other;
        return getID() == anotherFishAnnotation.getID();

    }
}
