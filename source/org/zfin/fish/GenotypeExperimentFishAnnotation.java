package org.zfin.fish;

import org.zfin.fish.presentation.Fish;
import org.zfin.mutant.GenotypeExperiment;

public class GenotypeExperimentFishAnnotation {

    private long id;
    private GenotypeExperiment genotypeExperiment;
    private FishAnnotation fishAnnotation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
    }

    public FishAnnotation getFishAnnotation() {
        return fishAnnotation;
    }

    public void setFishAnnotation(FishAnnotation fishAnnotation) {
        this.fishAnnotation = fishAnnotation;
    }

}
