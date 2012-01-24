package org.zfin.fish;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FunctionalAnnotation {

    private long ID;
    private String genotypeID;
    private String genotypeExperimentName;
    private String featureGroupName;
    private String morpholinoGroupName;
    private String name;
    private String phenotypeFigureGroupName;
    private int phenotypeFigureCount;
    private FeatureGroup featureGroup;

    //todo: probably remove these, they're around mostly to help develop the sorting algorithm
    private String geneOrFeatureText;
    private String scoringText;

    public String getGenotypeExperimentName() {
        return genotypeExperimentName;
    }

    public void setGenotypeExperimentName(String genotypeExperimentName) {
        this.genotypeExperimentName = genotypeExperimentName;
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

    public String getMorpholinoGroupName() {
        return morpholinoGroupName;
    }

    public void setMorpholinoGroupName(String morpholinoGroupName) {
        this.morpholinoGroupName = morpholinoGroupName;
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

    public FeatureGroup getFeatureGroup() {
        return featureGroup;
    }

    public void setFeatureGroup(FeatureGroup featureGroup) {
        this.featureGroup = featureGroup;
    }
}
