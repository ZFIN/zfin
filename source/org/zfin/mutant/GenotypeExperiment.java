package org.zfin.mutant;

import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;

import java.util.Set;

/**
 * Domain object.
 */
public class GenotypeExperiment {
    private String zdbID;
    private Experiment experiment;
    private Genotype genotype;
    private Set<PhenotypeExperiment> phenotypeExperiments;
    private Set<ExpressionExperiment> expressionExperiments;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
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

}
