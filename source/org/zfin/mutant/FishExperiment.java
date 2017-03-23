package org.zfin.mutant;

import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;

import java.util.Set;

/**
 * Domain object.
 */
public class FishExperiment implements Comparable<FishExperiment> {
    private String zdbID;
    private boolean standard;
    private boolean standardOrGenericControl;
    private Experiment experiment;
    private Fish fish;
    private Set<PhenotypeExperiment> phenotypeExperiments;
    private Set<ExpressionExperiment> expressionExperiments;
    private Set<GeneGenotypeExperiment> geneGenotypeExperiments;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public boolean isStandard() {
        return standard;
    }

    public void setStandard(boolean standard) {
        this.standard = standard;
    }

    public boolean isStandardOrGenericControl() {
        return standardOrGenericControl;
    }

    public void setStandardOrGenericControl(boolean standardOrGenericControl) {
        this.standardOrGenericControl = standardOrGenericControl;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public Set<ExpressionExperiment> getExpressionExperiments() {
        return expressionExperiments;
    }

    public void setExpressionExperiments(Set<ExpressionExperiment> expressionExperiments) {
        this.expressionExperiments = expressionExperiments;
    }

    public Set<PhenotypeExperiment> getPhenotypeExperiments() {
        return phenotypeExperiments;
    }

    public void setPhenotypeExperiments(Set<PhenotypeExperiment> phenotypeExperiments) {
        this.phenotypeExperiments = phenotypeExperiments;
    }

    @Override
    public int compareTo(FishExperiment o) {
        int fishCompare = fish.compareTo(o.getFish());
        if (fishCompare != 0) {
            return fishCompare;
        }
        return experiment.compareTo(o.getExperiment());
    }

    public Set<GeneGenotypeExperiment> getGeneGenotypeExperiments() {
        return geneGenotypeExperiments;
    }

    public void setGeneGenotypeExperiments(Set<GeneGenotypeExperiment> geneGenotypeExperiments) {
        this.geneGenotypeExperiments = geneGenotypeExperiments;
    }
}
