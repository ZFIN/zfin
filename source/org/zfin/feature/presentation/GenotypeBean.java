package org.zfin.feature.presentation;

import org.zfin.expression.*;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.fish.presentation.AbstractFishViewBean;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.PreviousNameLight;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.*;

// TODO: looks like a lot of copied code in GenotypeBean, AbstractFishViewBean, and GenotypeExperimentBean. Can some be refactored out?

public class GenotypeBean  extends AbstractFishViewBean{
    private Genotype genotype;
    private Fish fish;
    private FishStatistics fishStatistics;
    private List<GenotypeFeature> genotypeFeatures;
    private List<GenotypeFigure> genotypeFigures;
    private List<PhenotypeStatementWarehouse> phenoStatements;
    private List<ExpressionStatement> expressionStatements;
    private List<PhenotypeDisplay> phenoDisplays;
    private List<PreviousNameLight> previousNames;

    public String getFishName() {
        return fishName;
    }

    public void setFishName(String fishName) {
        this.fishName = fishName;
    }

    private int totalNumberOfPublications;
    private int totalNumberOfPhenotypes;
    private List<SequenceTargetingReagent> sequenceTargetingReagents;
    private String fishName;

    public List<SequenceTargetingReagent> getSequenceTargetingReagents() {
        return sequenceTargetingReagents;
    }

    public void setSequenceTargetingReagents(List<SequenceTargetingReagent> sequenceTargetingReagents) {
        this.sequenceTargetingReagents = sequenceTargetingReagents;
    }

    public GenotypeBean() {

    }

    public Genotype getGenotype() {
        if (genotype == null) {
            genotype = new Genotype();
        }
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public FishStatistics getFishStatistics() {
        if (fishStatistics == null) {
            if (fish == null) {
                return null;
            }
            return new FishStatistics(fish);
        }
        return fishStatistics;
    }

    public void setFishStatistics(FishStatistics fishStatistics) {
        this.fishStatistics = fishStatistics;
    }

    public List<GenotypeFeature> getGenotypeFeatures() {
        return genotypeFeatures;
    }

    public void setGenotypeFeatures(List<GenotypeFeature> genotypeFeatures) {
        this.genotypeFeatures = genotypeFeatures;
    }

    public List<GenotypeFigure> getGenotypeFigures() {
        return genotypeFigures;
    }

    public void setGenotypeFigures(List<GenotypeFigure> genotypeFigures) {
        this.genotypeFigures = genotypeFigures;
    }

    public List<PhenotypeStatementWarehouse> getPhenoStatements() {
        return phenoStatements;
    }

    public void setPhenoStatements(List<PhenotypeStatementWarehouse> phenoStatements) {
        this.phenoStatements = phenoStatements;
    }

    public List<ExpressionStatement> getExpressionStatements() {
        return expressionStatements;
    }

    public void setExpressionStatements(List<ExpressionStatement> expressionStatements) {
        this.expressionStatements = expressionStatements;
    }

    public List<PhenotypeDisplay> getPhenoDisplays() {
        if (phenoStatements == null) {
            return null;
        }

        return PhenotypeService.getPhenotypeDisplays(phenoStatements,"condition", "phenotypeStatement");
    }

    public void setPhenoDisplays(List<PhenotypeDisplay> phenoDisplays) {
        this.phenoDisplays = phenoDisplays;
    }

    public int getNumberOfPhenoDisplays() {
        if (phenoStatements == null || phenoStatements.size() == 0) {
            return 0;
        } else {
            if (phenoDisplays == null) {
                phenoDisplays = PhenotypeService.getPhenotypeDisplays(phenoStatements, "condition", "phenotypeStatement");
            }

            if (phenoDisplays == null) {
                return 0;
            } else {
                return phenoDisplays.size();
            }
        }
    }

    public int getTotalNumberOfPublications() {
        return totalNumberOfPublications;
    }

    public void setTotalNumberOfPublications(int totalNumberOfPublications) {
        this.totalNumberOfPublications = totalNumberOfPublications;
    }

    public int getTotalNumberOfPhenotypes() {
        return totalNumberOfPhenotypes;
    }

    public void setTotalNumberOfPhenotypes(int totalNumberOfPhenotypes) {
        this.totalNumberOfPhenotypes = totalNumberOfPhenotypes;
    }

    public List<PreviousNameLight> getPreviousNames() {
        return previousNames;
    }

    public void setPreviousNames(List<PreviousNameLight> previousNames) {
        this.previousNames = previousNames;
    }
}
