package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.marker.ExpressedGene;
import org.zfin.publication.Publication;

public class FigureExpressionSummaryDisplay implements Comparable<FigureExpressionSummaryDisplay> {
    private Figure figure;
    private ExpressedGene expressedGene;

    private boolean publicationDisplayed;

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


}
