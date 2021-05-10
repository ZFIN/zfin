package org.zfin.expression.presentation;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.List;

public class GeneResult extends ExpressionSearchResult {

    private String symbol;
    private Integer publicationCount;
    private Publication singlePublication;
    private Integer figureCount;
    private Figure singleFigure;
    private boolean hasImage;
    private Marker gene;
    private String matchingText;
    private String figureResultUrl;
    private String expressionId;

    public String getUrl() { return "http://zfin.org/" + getId(); }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getPublicationCount() {
        return publicationCount;
    }

    public void setPublicationCount(Integer publicationCount) {
        this.publicationCount = publicationCount;
    }

    public Publication getSinglePublication() {
        return singlePublication;
    }

    public void setSinglePublication(Publication singlePublication) {
        this.singlePublication = singlePublication;
    }

    public Integer getFigureCount() {
        return figureCount;
    }

    public void setFigureCount(Integer figureCount) {
        this.figureCount = figureCount;
    }

    public Figure getSingleFigure() {
        return singleFigure;
    }

    public void setSingleFigure(Figure singleFigure) {
        this.singleFigure = singleFigure;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
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

    public String getFigureResultUrl() {
        return figureResultUrl;
    }

    public void setFigureResultUrl(String figureResultUrl) {
        this.figureResultUrl = figureResultUrl;
    }

    public String getExpressionId() {
        return expressionId;
    }

    public void setExpressionId(String expressionId) {
        this.expressionId = expressionId;
    }


}
