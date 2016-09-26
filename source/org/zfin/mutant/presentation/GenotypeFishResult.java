package org.zfin.mutant.presentation;

import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;

import java.util.List;

/**
 * This class is a statistics class about Fish for given genotype
 */
public class GenotypeFishResult implements Comparable<GenotypeFishResult> {

    private Fish fish;
    private List<Marker> affectedMarkers;
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

    public List<Marker> getAffectedMarkers() {
        return affectedMarkers;
    }

    public void setAffectedMarkers(List<Marker> affectedMarkers) {
        this.affectedMarkers = affectedMarkers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenotypeFishResult that = (GenotypeFishResult) o;

        return fish.equals(that.fish);

    }

    @Override
    public int hashCode() {
        return fish.hashCode();
    }

    @Override
    public int compareTo(GenotypeFishResult o) {
        return fish.compareTo(o.getFish());
    }
}
