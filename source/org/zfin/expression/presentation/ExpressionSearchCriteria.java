package org.zfin.expression.presentation;


import org.zfin.marker.Marker;
import org.zfin.util.URLCreator;

import java.util.List;

public class ExpressionSearchCriteria {

    private String geneField;
    private String geneZdbID;

    private List<String> anatomy;

    private List<GeneResult> geneResults;
    private List<FigureResult> figureResults;
    private List<ImageResult> imageResults;

    private Long numFound;
    private Integer rows;
    private Integer page;

    public String getUrl(Marker gene) {
        //todo: put all of the criteria into the url

        URLCreator urlCreator = new URLCreator("/action/expression/results");
        urlCreator.addNamevaluePair("geneZdbID", gene.getZdbID());
        urlCreator.addNamevaluePair("geneField", gene.getAbbreviation());

        return urlCreator.getURL();
    }


    public String getGeneField() {
        return geneField;
    }

    public void setGeneField(String geneField) {
        this.geneField = geneField;
    }

    public void setGeneZdbID(String geneZdbID) {
        this.geneZdbID = geneZdbID;
    }


    public String getGeneZdbID() {
        return geneZdbID;
    }

    public void setGeneZdbId(String geneZdbID) {
        this.geneZdbID = geneZdbID;
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

    public List<ImageResult> getImageResults() {
        return imageResults;
    }

    public void setImageResults(List<ImageResult> imageResults) {
        this.imageResults = imageResults;
    }

    public Long getNumFound() {
        return numFound;
    }

    public void setNumFound(Long numFound) {
        this.numFound = numFound;
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
