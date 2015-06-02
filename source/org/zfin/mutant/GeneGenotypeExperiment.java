package org.zfin.mutant;

import org.zfin.marker.Marker;

import java.io.Serializable;

/**
 * Created by cmpich on 4/22/14.
 */
public class GeneGenotypeExperiment implements Serializable {

    private Marker gene;
    private FishExperiment fishExperiment;

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneGenotypeExperiment that = (GeneGenotypeExperiment) o;

        if (gene != null ? !gene.equals(that.gene) : that.gene != null) return false;
        if (fishExperiment != null ? !fishExperiment.equals(that.fishExperiment) : that.fishExperiment != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = gene != null ? gene.hashCode() : 0;
        result = 31 * result + (fishExperiment != null ? fishExperiment.hashCode() : 0);
        return result;
    }
}
