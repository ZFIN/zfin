package org.zfin.expression.presentation;


import org.zfin.marker.Marker;
import org.zfin.util.URLCreator;

import java.util.Arrays;
import java.util.List;

public class ExpressionSearchCriteria {

    private String geneField;
    private String geneZdbID;
    private Marker gene;
    private String targetGeneField;
    private boolean onlyFiguresWithImages;
    private boolean onlyWildtype;
    private String startStageId;
    private String endStageId;
    private String assayName;
    private String fish;

    private String anatomyTermNames;
    private String anatomyTermIDs;

    private List<GeneResult> geneResults;
    private List<FigureResult> figureResults;
    private List<ImageResult> imageResults;

    private long numFound;
    private long pubCount;
    private Integer rows;
    private Integer page;

    private String linkWithImagesOnly;

    public String getUrl(Marker gene) {
        //todo: put all of the criteria into the url

        URLCreator urlCreator = new URLCreator("/action/expression/results");
        urlCreator.addNameValuePair("geneZdbID", gene.getZdbID());
        urlCreator.addNameValuePair("geneField", gene.getAbbreviation());
        urlCreator.addNameValuePair("anatomyTermNames", getAnatomyTermNames());
        urlCreator.addNameValuePair("anatomyTermIDs", getAnatomyTermIDs());
        urlCreator.addNameValuePair("startStageId", getStartStageId());
        urlCreator.addNameValuePair("endStageId", getEndStageId());
        urlCreator.addNameValuePair("targetGeneField", getTargetGeneField());
        urlCreator.addNameValuePair("assayName", getAssayName());
        urlCreator.addNameValuePair("fish", getFish());
        if (onlyFiguresWithImages) {
            urlCreator.addNameValuePair("onlyFiguresWithImages", "true");
        }
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

    public String getTargetGeneField() {
        return targetGeneField;
    }

    public void setTargetGeneField(String targetGeneField) {
        this.targetGeneField = targetGeneField;
    }

    public boolean isOnlyFiguresWithImages() {
        return onlyFiguresWithImages;
    }

    public void setOnlyFiguresWithImages(boolean onlyFiguresWithImages) {
        this.onlyFiguresWithImages = onlyFiguresWithImages;
    }

    public boolean isOnlyWildtype() {
        return onlyWildtype;
    }

    public void setOnlyWildtype(boolean onlyWildtype) {
        this.onlyWildtype = onlyWildtype;
    }

    public String getStartStageId() {
        return startStageId;
    }

    public void setStartStageId(String startStageId) {
        this.startStageId = startStageId;
    }

    public String getEndStageId() {
        return endStageId;
    }

    public void setEndStageId(String endStageId) {
        this.endStageId = endStageId;
    }

    public String getAssayName() {
        return assayName;
    }

    public void setAssayName(String assayName) {
        this.assayName = assayName;
    }

    public String getFish() {
        return fish;
    }

    public void setFish(String fish) {
        this.fish = fish;
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

    public String getLinkWithImagesOnly() {
        return linkWithImagesOnly;
    }

    public void setLinkWithImagesOnly(String linkWithImagesOnly) {
        this.linkWithImagesOnly = linkWithImagesOnly;
    }
}
