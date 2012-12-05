package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;

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



    private List<PhenotypeStatement> phenotypeStatementList;
    public List<String> geno;

    public List<String> getGeno() {
        return geno;
    }

    public void setGeno(List<String> geno) {
        this.geno = geno;
    }
    // for expression summary page


    public List<Genotype> getGenotype() {
        return genotype;
    }

    public void setGenotype(List<Genotype> genotype) {
        this.genotype = genotype;
    }

    private List<ExpressionStatement> expressionStatementList;
    private DevelopmentStage earliestStartStage;
    private DevelopmentStage latestEndStage;
    private List<Marker> expressedGenes;
    private List<Genotype> genotype;

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

    public List<ExpressionStatement> getExpressionStatementList() {
        return expressionStatementList;
    }

    public void setExpressionStatementList(List<ExpressionStatement> expressionStatementList) {
        this.expressionStatementList = expressionStatementList;
    }

    public List<PhenotypeStatement> getPhenotypeStatementList() {
        return phenotypeStatementList;
    }

    public void setPhenotypeStatementList(List<PhenotypeStatement> phenotypeStatementList) {
        this.phenotypeStatementList = phenotypeStatementList;
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

    public void addPhenotypeStatement(PhenotypeStatement statement) {
        if (phenotypeStatementList == null)
            phenotypeStatementList = new ArrayList<PhenotypeStatement>();
        phenotypeStatementList.add(statement);
    }

}
