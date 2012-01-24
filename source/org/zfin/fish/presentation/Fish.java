package org.zfin.fish.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.MutationType;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.infrastructure.ZfinFigureEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Zfin Entity that only exists in the warehouse context at this time.
 * It does only have minimal data that is needed for the search result page,
 * and thus does not represent a full Zfin entity.
 */
public class Fish extends ZfinEntity {

    private ZfinEntity genotype;
    private List<String> genotypeExperimentIDs;
    private String genotypeExperimentIDsString;
    private List<ZfinEntity> features;
    private List<ZfinEntity> morpholinos;
    private List<ZfinEntity> affectedGenes;
    private List<FeatureGene> featureGenes = new ArrayList<FeatureGene>();
    private int phenotypeFigureCount;
    private boolean imageAvailable;
    //todo: probably remove this once we have real matching text
    private String geneOrFeatureText;
    private String scoringText;

    private List<String> mutationTypes;
    private Set<ZfinFigureEntity> phenotypeFigures;

    public String getFishID() {
        if (getGenotypeID() == null)
            throw new NullPointerException("No GENO id found for fish ID: " + ID);
        String fishID = getGenotypeID();
        if (genotypeExperimentIDsString != null)
            return fishID + "," + genotypeExperimentIDsString;
        return fishID;
    }

    public ZfinEntity getGenotype() {
        return genotype;
    }

    public void setGenotype(ZfinEntity genotype) {
        this.genotype = genotype;
    }

    public String getGenotypeExperimentIDsString() {
        return genotypeExperimentIDsString;
    }

    public void setGenotypeExperimentIDsString(String genotypeExperimentIDsString) {
        this.genotypeExperimentIDsString = genotypeExperimentIDsString;
    }

    public List<ZfinEntity> getFeatures() {
        if (CollectionUtils.isEmpty(featureGenes))
            return null;

        if (features != null)
            return features;

        features = new ArrayList<ZfinEntity>(featureGenes.size());
        for (FeatureGene featureGene : featureGenes)
            if (!featureGene.getMutationTypeDisplay().equals(MutationType.MORPHOLINO))
                features.add(featureGene.getFeature());
        return features;
    }

    public void setFeatures(List<ZfinEntity> features) {
        this.features = features;
    }

    public List<ZfinEntity> getMorpholinos() {
        if (CollectionUtils.isEmpty(featureGenes))
            return null;

        if (morpholinos != null)
            return morpholinos;

        morpholinos = new ArrayList<ZfinEntity>(featureGenes.size());
        for (FeatureGene featureGene : featureGenes) {
            if (featureGene.getMutationTypeDisplay().equals(MutationType.MORPHOLINO))
                morpholinos.add(featureGene.getFeature());
        }
        return morpholinos;
    }

    public void setMorpholinos(List<ZfinEntity> morpholinos) {
        this.morpholinos = morpholinos;
    }

    public List<ZfinEntity> getAffectedGenes() {
        if (CollectionUtils.isEmpty(featureGenes))
            return null;

        if (affectedGenes != null)
            return affectedGenes;

        affectedGenes = new ArrayList<ZfinEntity>(featureGenes.size());
        for (FeatureGene featureGene : featureGenes)
            affectedGenes.add(featureGene.getGene());
        return affectedGenes;
    }

    public List<String> getMutationTypes() {
        return mutationTypes;
    }

    public void setMutationTypes(List<String> mutationTypes) {
        this.mutationTypes = mutationTypes;
    }

    public Set<ZfinFigureEntity> getPhenotypeFigures() {
        return phenotypeFigures;
    }

    public ZfinFigureEntity getSingleFigure() {
        if (phenotypeFigures == null || phenotypeFigures.size() > 1)
            throw new RuntimeException("Did not find exactly one figure for fish " + getFishID());
        return phenotypeFigures.iterator().next();
    }

    public void setPhenotypeFigures(Set<ZfinFigureEntity> phenotypeFigures) {
        this.phenotypeFigures = phenotypeFigures;
        phenotypeFigureCount = phenotypeFigures.size();
    }

    public List<FeatureGene> getFeatureGenes() {
        return featureGenes;
    }

    public void setFeatureGenes(List<FeatureGene> featureGenes) {
        this.featureGenes = featureGenes;
    }

    public int getPhenotypeFigureCount() {
        return phenotypeFigureCount;
    }

    public void setPhenotypeFigureCount(int phenotypeFigureCount) {
        this.phenotypeFigureCount = phenotypeFigureCount;
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

    public void addFeatureGene(FeatureGene geneFeature) {
        featureGenes.add(geneFeature);
    }

    public List<String> getGenotypeExperimentIDs() {
        return genotypeExperimentIDs;
    }

    public void setGenotypeExperimentIDs(List<String> genotypeExperimentIDs) {
        this.genotypeExperimentIDs = genotypeExperimentIDs;
    }

    public String getGenotypeID() {
        if (genotype != null)
            return genotype.getID();
        else return null;
    }

    public boolean isImageAvailable() {
        return imageAvailable;
    }

    public void setImageAvailable(boolean imageAvailable) {
        this.imageAvailable = imageAvailable;
    }
}
