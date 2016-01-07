package org.zfin.expression;

import org.zfin.marker.Marker;
import org.zfin.mutant.FishExperiment;

/**
 * Created by prita on 12/23/2015.
 */
public class CleanExpFastSrch {
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private Marker gene;

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

    private FishExperiment fishExperiment;
}
