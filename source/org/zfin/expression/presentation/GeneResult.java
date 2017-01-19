package org.zfin.expression.presentation;

public class GeneResult {

    String symbol;
    String id;
    Integer publicationCount;
    Integer figureCount;

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
}
