package org.zfin.expression.presentation;

import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.Figure;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.publication.Publication;

import java.util.List;

public class FigureSummaryDisplay implements Comparable<FigureSummaryDisplay> {
    private Publication publication;
    private Figure figure;
    private int imgCount;
    private String thumbnail;
    private List<ExpressionStatement> expressionStatementList;
    private List<PhenotypeStatement> phenotypeStatementList;

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

    public int compareTo(FigureSummaryDisplay anotherFigureSummary) {
        if (anotherFigureSummary == null)
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

}
