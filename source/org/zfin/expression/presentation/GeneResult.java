package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.marker.Marker;

public class GeneResult {

    String symbol;
    String id;
    Integer publicationCount;
    Integer figureCount;
    Marker gene;
    String matchingText;
    DevelopmentStage startStage;
    DevelopmentStage endStage;


    public String getUrl() { return "http://zfin.org/" + id; }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPublicationCount() {
        return publicationCount;
    }

    public void setPublicationCount(Integer publicationCount) {
        this.publicationCount = publicationCount;
    }

    public Integer getFigureCount() {
        return figureCount;
    }

    public void setFigureCount(Integer figureCount) {
        this.figureCount = figureCount;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public String getMatchingText() {
        return matchingText;
    }

    public void setMatchingText(String matchingText) {
        this.matchingText = matchingText;
    }

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }
}
