package org.zfin.mutant;

import org.zfin.marker.Marker;

import java.io.Serializable;

/**
 * Created by cmpich on 4/22/14.
 */
public class GeneGenotypeExperiment implements Serializable {

    private Marker gene;
    private GenotypeExperiment genotypeExperiment;

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
    }
}
