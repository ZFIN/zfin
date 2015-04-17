package org.zfin.mutant;

import org.zfin.expression.Experiment;
import org.zfin.ontology.GenericTerm;

/**
 * Created by cmpich on 3/31/15.
 */
public class DiseaseModel {

    private GenericTerm term;
    private Genotype genotype;
    private Experiment experiment;

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

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }
}
