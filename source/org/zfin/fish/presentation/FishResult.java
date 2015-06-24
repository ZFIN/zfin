package org.zfin.fish.presentation;

import org.zfin.fish.FeatureGene;
import org.zfin.mutant.Fish;
import org.zfin.search.presentation.SearchResult;

import java.util.List;


public class FishSearchResult extends SearchResult {

    Fish fish;

    List<FeatureGene> featureGenes;

    Integer expressionFigureCount;
    Boolean expressionImageAvailable;

    Integer phenotypeFigureCount;
    Boolean phenotypeImageAvailable;

    Boolean imageAvailable;

}
