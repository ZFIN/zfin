package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.marker.ExpressedGene;
import org.zfin.mutant.ExpressedGenotype;
import org.zfin.mutant.Genotype;
import org.zfin.publication.Publication;

public class FigureExpressionSummaryDisplay implements Comparable<FigureExpressionSummaryDisplay> {
    private Figure figure;
    private ExpressedGene expressedGene;
    private Genotype expressedGenotype;

    private boolean publicationDisplayed;

    public Genotype getExpressedGenotype() {
        return expressedGenotype;
    }

    public void setExpressedGenotype(Genotype expressedGenotype) {
        this.expressedGenotype = expressedGenotype;
    }

    public FigureExpressionSummaryDisplay(Figure figure) {

        this.figure = figure;
    }

    public Publication getPublication() {
        return figure.getPublication();
    }

    public Figure getFigure() {
        return figure;
    }

    public ExpressedGene getExpressedGene() {
        return expressedGene;
    }

    public void setExpressedGene(ExpressedGene expressedGene) {
        this.expressedGene = expressedGene;
    }

    public String getThumbnail() {
        if (figure.isImgless())
            return null;
        return figure.getImages().iterator().next().getThumbnail();
    }

    public int compareTo(FigureExpressionSummaryDisplay anotherFigureSummaryDisplay) {
        if (anotherFigureSummaryDisplay == null)
            return 1;
        int compareResult = getPublication().compareTo(anotherFigureSummaryDisplay.getPublication());
        if (compareResult != 0)
            return compareResult;
        compareResult = figure.getLabel().compareTo(anotherFigureSummaryDisplay.getFigure().getLabel());
        if (compareResult != 0)
            return compareResult;
        return expressedGene.getGene().compareTo(anotherFigureSummaryDisplay.getExpressedGene().getGene());
    }

    public int getImgCount() {
        return figure.getImages().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FigureExpressionSummaryDisplay that = (FigureExpressionSummaryDisplay) o;

        if (expressedGene != null ? !expressedGene.getGene().equals(that.expressedGene.getGene()) : that.expressedGene != null)
            return false;
        if (figure != null ? !figure.equals(that.figure) : that.figure != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = figure != null ? figure.hashCode() : 0;
        result = 31 * result + (expressedGene != null ? expressedGene.getGene().hashCode() : 0);
        return result;
    }
}
