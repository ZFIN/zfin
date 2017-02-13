package org.zfin.expression.presentation;


import org.zfin.marker.Marker;
import org.zfin.util.URLCreator;

import java.util.Arrays;
import java.util.List;

public class ExpressionSearchCriteria {

    private String geneField;
    private String geneZdbID;
    private Marker gene;

    private String anatomyTermNames;
    private String anatomyTermIDs;

    private List<GeneResult> geneResults;
    private List<FigureResult> figureResults;
    private List<ImageResult> imageResults;

    private long numFound;
    private long pubCount;
    private Integer rows;
    private Integer page;

    public String getUrl(Marker gene) {
        //todo: put all of the criteria into the url

        URLCreator urlCreator = new URLCreator("/action/expression/results");
        urlCreator.addNamevaluePair("geneZdbID", gene.getZdbID());
        urlCreator.addNamevaluePair("geneField", gene.getAbbreviation());

        return urlCreator.getURL();
    }

    public List<String> getAnatomy() {
        if (anatomyTermNames == null || anatomyTermNames.equals("")) { return null; }
        return Arrays.asList(anatomyTermNames.split("\\|"));
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

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public String getAnatomyTermNames() {
        return anatomyTermNames;
    }

    public void setAnatomyTermNames(String anatomyTermNames) {
        this.anatomyTermNames = anatomyTermNames;
    }

    public String getAnatomyTermIDs() {
        return anatomyTermIDs;
    }

    public void setAnatomyTermIDs(String anatomyTermIDs) {
        this.anatomyTermIDs = anatomyTermIDs;
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

    public long getNumFound() {
        return numFound;
    }

    public void setNumFound(long numFound) {
        this.numFound = numFound;
    }

    public long getPubCount() {
        return pubCount;
    }

    public void setPubCount(long pubCount) {
        this.pubCount = pubCount;
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
