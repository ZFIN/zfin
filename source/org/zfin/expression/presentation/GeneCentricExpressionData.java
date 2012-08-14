package org.zfin.expression.presentation;

import org.zfin.framework.presentation.FigureData;
import org.zfin.marker.ExpressedGene;

public class GeneCentricExpressionData implements Comparable<GeneCentricExpressionData> {

    private ExpressedGene expressedGene;
    private FigureData figureData;

    public GeneCentricExpressionData(ExpressedGene expressedGene) {
        this.expressedGene = expressedGene;
    }

    public ExpressedGene getExpressedGene() {
        return expressedGene;
    }

    public int compareTo(GeneCentricExpressionData anotherFigureSummaryDisplay) {
        if (anotherFigureSummaryDisplay == null)
            return 1;
        return expressedGene.getGene().compareTo(anotherFigureSummaryDisplay.getExpressedGene().getGene());
    }

    public FigureData getFigureData() {
        return figureData;
    }

    public void setFigureData(FigureData figureData) {
        this.figureData = figureData;
    }

}
