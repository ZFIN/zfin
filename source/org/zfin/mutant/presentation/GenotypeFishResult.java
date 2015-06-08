package org.zfin.mutant.presentation;

import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;

import java.util.List;

/**
 * This class is a statistics class about Fish for given genotype
 */
public class GenotypeFishResult {

    private Fish fish;
    private FishGenotypePhenotypeStatistics fishGenotypePhenotypeStatistics;
    private FishGenotypeExpressionStatistics fishGenotypeExpressionStatistics;

    public GenotypeFishResult(Fish fish) {
        this.fish = fish;
    }

    public Fish getFish() {
        return fish;
    }

    public FishGenotypeExpressionStatistics getFishGenotypeExpressionStatistics() {
        return fishGenotypeExpressionStatistics;
    }

    public void setFishGenotypeExpressionStatistics(FishGenotypeExpressionStatistics fishGenotypeExpressionStatistics) {
        this.fishGenotypeExpressionStatistics = fishGenotypeExpressionStatistics;
    }

    public FishGenotypePhenotypeStatistics getFishGenotypePhenotypeStatistics() {
        return fishGenotypePhenotypeStatistics;
    }

    public void setFishGenotypePhenotypeStatistics(FishGenotypePhenotypeStatistics fishGenotypePhenotypeStatistics) {
        this.fishGenotypePhenotypeStatistics = fishGenotypePhenotypeStatistics;
    }

    public List<? extends Marker> getAffectedMarker() {
        return fish.getStrList();
    }


}
