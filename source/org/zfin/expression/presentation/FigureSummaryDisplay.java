package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FigureSummaryDisplay implements Comparable<FigureSummaryDisplay> {

    private Publication publication;
    private Figure figure;
    private int imgCount;

    public List<Experiment> getExp() {
        return exp;
    }

    public void setExp(List<Experiment> exp) {
        this.exp = exp;
    }

    private String thumbnail;
    private List<Experiment> exp;

    // for phenotype summary page
    private Set<PhenotypeStatementWarehouse> phenotypeStatementList;
    public List<String> geno;

    public List<String> getGeno() {
        return geno;
    }

    public void setGeno(List<String> geno) {
        this.geno = geno;
    }
    // for expression summary page


    private List<ExpressionStatement> expressionStatementList;
    private DevelopmentStage earliestStartStage;
    private DevelopmentStage latestEndStage;
    private List<Marker> expressedGenes;
    private Set<Fish> fishList;

    private boolean publicationDisplayed;

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isPublicationDisplayed() {
        return publicationDisplayed;
    }

    public void setPublicationDisplayed(boolean publicationDisplayed) {
        this.publicationDisplayed = publicationDisplayed;
    }

    public List<Fish> getFishList() {
        if (fishList == null)
            return null;
        return new ArrayList<>(fishList);
    }

    public void setFishList(List<Fish> fish) {
        if (fish != null)
            fishList = new HashSet<>(fish);
    }

    public void addFish(Fish fish) {
        if (fishList == null)
            fishList = new HashSet<>();
        fishList.add(fish);
    }

    public List<ExpressionStatement> getExpressionStatementList() {
        return expressionStatementList;
    }

    public void setExpressionStatementList(List<ExpressionStatement> expressionStatementList) {
        this.expressionStatementList = expressionStatementList;
    }

    public List<PhenotypeStatementWarehouse> getPhenotypeStatementList() {
        if (phenotypeStatementList == null)
            return null;
        return new ArrayList<>(phenotypeStatementList);
    }

    public void setPhenotypeStatementList(List<PhenotypeStatementWarehouse> phenotypeStatementList) {
        if (this.phenotypeStatementList == null)
            this.phenotypeStatementList = new HashSet<>();
        this.phenotypeStatementList.addAll(phenotypeStatementList);
    }

    public List<Marker> getExpressedGenes() {
        return expressedGenes;
    }

    public void setExpressedGenes(List<Marker> expressedGenes) {
        this.expressedGenes = expressedGenes;
    }

    public DevelopmentStage getEarliestStartStage() {
        return earliestStartStage;
    }

    public void setEarliestStartStage(DevelopmentStage earliestStartStage) {
        this.earliestStartStage = earliestStartStage;
    }

    public DevelopmentStage getLatestEndStage() {
        return latestEndStage;
    }

    public void setLatestEndStage(DevelopmentStage latestEndStage) {
        this.latestEndStage = latestEndStage;
    }


    public int compareTo(FigureSummaryDisplay anotherFigureSummary) {
        if (anotherFigureSummary.publication == null)
            return 1;
        int compareResult = publication.compareTo(anotherFigureSummary.getPublication());
        if (compareResult == 0) {
            return figure.getLabel().compareTo(anotherFigureSummary.getFigure().getLabel());
        } else {
            return compareResult;
        }
    }

    public int getImgCount() {
        if (figure == null)
            return 0;
        return figure.getImages().size();
    }

    public void setImgCount(int imgCount) {
        this.imgCount = imgCount;
    }

    public void addPhenotypeStatement(PhenotypeStatementWarehouse statement) {
        if (phenotypeStatementList == null)
            phenotypeStatementList = new HashSet<>();
        phenotypeStatementList.add(statement);
    }

}
