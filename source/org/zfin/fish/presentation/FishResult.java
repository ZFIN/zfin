package org.zfin.fish.presentation;

import org.zfin.fish.FeatureGene;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.Fish;
import org.zfin.search.presentation.SearchResult;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class FishResult extends SearchResult {

    private Fish fish;

    //maybe this belongs directly on the fish?
    private List<FeatureGene> featureGenes;

    private Integer expressionFigureCount;
    private Boolean expressionImageAvailable;

    private Boolean phenotypeImageAvailable;

    private String geneOrFeatureText;
    private String scoringText;

    private Set<ZfinFigureEntity> phenotypeFigures;
    private Boolean imageAvailable;


    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public List<FeatureGene> getFeatureGenes() {
        return featureGenes;
    }

    public void setFeatureGenes(List<FeatureGene> featureGenes) {
        this.featureGenes = featureGenes;
    }

    public Integer getExpressionFigureCount() {
        return expressionFigureCount;
    }

    public void setExpressionFigureCount(Integer expressionFigureCount) {
        this.expressionFigureCount = expressionFigureCount;
    }

    public Boolean getExpressionImageAvailable() {
        return expressionImageAvailable;
    }

    public void setExpressionImageAvailable(Boolean expressionImageAvailable) {
        this.expressionImageAvailable = expressionImageAvailable;
    }

    public Integer getPhenotypeFigureCount() {
        if (phenotypeFigures == null) { return 0; }
        return phenotypeFigures.size();
    }

    public Boolean getPhenotypeImageAvailable() {
        return phenotypeImageAvailable;
    }

    public void setPhenotypeImageAvailable(Boolean phenotypeImageAvailable) {
        this.phenotypeImageAvailable = phenotypeImageAvailable;
    }

    public Boolean getImageAvailable() {
        return imageAvailable;
    }

    public void setImageAvailable(Boolean imageAvailable) {
        this.imageAvailable = imageAvailable;
    }

    public String getGeneOrFeatureText() {
        return geneOrFeatureText;
    }

    public void setGeneOrFeatureText(String geneOrFeatureText) {
        this.geneOrFeatureText = geneOrFeatureText;
    }

    public String getScoringText() {
        return scoringText;
    }

    public void setScoringText(String scoringText) {
        this.scoringText = scoringText;
    }

    public Set<ZfinFigureEntity> getPhenotypeFigures() {
        return phenotypeFigures;
    }

    public void setPhenotypeFigures(Set<ZfinFigureEntity> phenotypeFigures) {
        this.phenotypeFigures = phenotypeFigures;
    }

    public ZfinFigureEntity getSingleFigure() {
        if (phenotypeFigures == null || phenotypeFigures.size() > 1)
            throw new RuntimeException("Did not find exactly one figure for fish " + fish.getZdbID());
        return phenotypeFigures.iterator().next();
    }

}
