package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.ExpressedGenotype;
import org.zfin.mutant.Genotype;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;

public class FigureExpressionSummary implements Comparable<FigureExpressionSummary> {
    private Figure figure;
    private String thumbnail;
    private List<ExpressedGene> expressedGenes;
    private Genotype expressedGenotypes;


    public Genotype getExpressedGenotypes() {
        return expressedGenotypes;
    }

    public void setExpressedGenotypes(Genotype expressedGenotypes) {
        this.expressedGenotypes = expressedGenotypes;
    }

    private DevelopmentStage earliestStartStage;


    private DevelopmentStage latestEndStage;

    private boolean publicationDisplayed;

    public FigureExpressionSummary(Figure figure) {
        this.figure = figure;
    }

    public Publication getPublication() {
        return figure.getPublication();
    }

    public Figure getFigure() {
        return figure;
    }

    public String getThumbnail() {
        if (figure.isImgless())
            return null;
        return figure.getImages().iterator().next().getThumbnail();
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

    public List<ExpressedGene> getExpressedGenes() {
        return expressedGenes;
    }

    public void setExpressedGenes(List<ExpressedGene> expressedGenes) {
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


    public int compareTo(FigureExpressionSummary anotherFigureSummary) {
        if (anotherFigureSummary == null)
            return 1;
        int compareResult = getPublication().compareTo(anotherFigureSummary.getPublication());
        if (compareResult == 0) {
            return figure.getLabel().compareTo(anotherFigureSummary.getFigure().getLabel());
        } else {
            return compareResult;
        }
    }

    public int getImgCount() {
        return figure.getImages().size();
    }

    public void addExpressionGene(ExpressedGene expressedGene) {
        if (expressedGenes == null)
            expressedGenes = new ArrayList<ExpressedGene>();
        expressedGenes.add(expressedGene);
    }
}
