package org.zfin.mutant;

import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;

import java.util.HashSet;
import java.util.Set;

/**
 * Domain object.
 */
public class GenotypeExperiment {
    private String zdbID;
    private Experiment experiment;
    private Genotype genotype;
    private Set<Phenotype> phenotypes;
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

    public Set<Phenotype> getPhenotypes() {
        if (phenotypes == null)
            return null;
        return phenotypes;
    }

    public void setPhenotypes(Set<Phenotype> phenotypes) {
        this.phenotypes = phenotypes;
    }

    public void addPhenotype(Phenotype phenotype) {
        if (phenotypes == null)
            phenotypes = new HashSet<Phenotype>(5);
        phenotypes.add(phenotype);
    }

}
