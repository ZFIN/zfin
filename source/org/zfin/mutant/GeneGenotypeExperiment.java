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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneGenotypeExperiment that = (GeneGenotypeExperiment) o;

        if (gene != null ? !gene.equals(that.gene) : that.gene != null) return false;
        if (genotypeExperiment != null ? !genotypeExperiment.equals(that.genotypeExperiment) : that.genotypeExperiment != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = gene != null ? gene.hashCode() : 0;
        result = 31 * result + (genotypeExperiment != null ? genotypeExperiment.hashCode() : 0);
        return result;
    }
}
