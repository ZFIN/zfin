package org.zfin.mutant;

import org.zfin.expression.Experiment;

import java.util.Set;

/**
 * Domain object.
 */
public class GenotypeExperiment {
    private String zdbID;
    private Experiment experiment;
    private Genotype genotype;
    private Set<Phenotype> phenotypes;


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

    public Set<Phenotype> getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(Set<Phenotype> phenotypes) {
        this.phenotypes = phenotypes;
    }
}
