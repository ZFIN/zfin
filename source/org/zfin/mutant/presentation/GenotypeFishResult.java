package org.zfin.mutant.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.GenotypeService;
import org.zfin.mutant.SequenceTargetingReagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public List<Marker> getAffectedMarker() {
        List<SequenceTargetingReagent> strList = fish.getStrList();
        if (CollectionUtils.isEmpty(strList))
            return null;
        List<Marker> geneList = new ArrayList<>(strList.size());
        Set<Marker> affectedMarkerOnGenotype = GenotypeService.getAffectedMarker(fish.getGenotype());
        if (affectedMarkerOnGenotype != null)
            geneList.addAll(affectedMarkerOnGenotype);
        for (SequenceTargetingReagent str : strList)
            geneList.addAll(str.getTargetGenes());
        return geneList;
    }


}
