package org.zfin.fish.presentation;

import org.zfin.expression.FigureExpressionSummary;
import org.zfin.fish.FeatureGene;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.Fish;
import org.zfin.search.presentation.SearchResult;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class FishResult extends SearchResult {

    private Fish fish;

    private Integer expressionFigureCount;
    private Boolean expressionImageAvailable;

    private Boolean phenotypeImageAvailable;

    private String geneOrFeatureText;
    private String scoringText;

    private Set<ZfinFigureEntity> phenotypeFigures;
    private  Set<ZfinFigureEntity> expressionFigures;

    public void setExpressionFigures(Set<ZfinFigureEntity> expressionFigures) {
        this.expressionFigures = expressionFigures;
    }

    private Boolean imageAvailable;


    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }


    public Integer getExpressionFigureCount() {
        if (expressionFigures == null) { return 0; }
        expressionFigureCount= expressionFigures.size();
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
