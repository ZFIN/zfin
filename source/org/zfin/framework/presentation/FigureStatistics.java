package org.zfin.framework.presentation;

import org.zfin.expression.Figure;

import java.util.Collection;

/**
 *
 */
public class FigureStatistics {

    private Collection<Figure> figures;
    private int textOnlyFigures;
    private int trueFigures;

    public FigureStatistics(int trueFigures, int textOnlyFigures) {
        this.textOnlyFigures = textOnlyFigures;
        this.trueFigures = trueFigures;
    }

    public boolean isOnlyTextOnlyFigures() {
        return trueFigures == 0 && textOnlyFigures > 0;
    }

    public boolean isAtLeastOneTrueFigure() {
        return trueFigures > 0;
    }

    public void setFigures(Collection<Figure> figures) {
        this.figures = figures;
    }

    public int getNumberOfFigures() {
        return trueFigures + textOnlyFigures;
    }
}
