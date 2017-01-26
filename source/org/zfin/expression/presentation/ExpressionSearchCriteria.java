package org.zfin.expression.presentation;


import java.util.List;

public class ExpressionSearchCriteria {

    String geneField;
    String exactGene;
    List<String> anatomy;

    List<GeneResult> geneResults;
    List<FigureResult> figureResults;

    public String getGeneField() {
        return geneField;
    }

    public void setGeneField(String geneField) {
        this.geneField = geneField;
    }

    public String getExactGene() {
        return exactGene;
    }

    public void setExactGene(String exactGene) {
        this.exactGene = exactGene;
    }

    public List<String> getAnatomy() {
        return anatomy;
    }

    public void setAnatomy(List<String> anatomy) {
        this.anatomy = anatomy;
    }

    public List<GeneResult> getGeneResults() {
        return geneResults;
    }

    public void setGeneResults(List<GeneResult> geneResults) {
        this.geneResults = geneResults;
    }

    public List<FigureResult> getFigureResults() {
        return figureResults;
    }

    public void setFigureResults(List<FigureResult> figureResults) {
        this.figureResults = figureResults;
    }
}
