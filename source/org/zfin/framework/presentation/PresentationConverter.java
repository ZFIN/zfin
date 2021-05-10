package org.zfin.framework.presentation;

import org.zfin.expression.FigureExpressionSummary;
import org.zfin.expression.presentation.FigureExpressionSummaryDisplay;
import org.zfin.expression.presentation.GeneCentricExpressionData;
import org.zfin.marker.ExpressedGene;
import org.zfin.marker.Marker;

import java.util.*;

/**
 * Utility to convert entity classes into presentation classes.
 */
public class PresentationConverter {

    // Convert list of FigureExpressionSummary records into unique FigureExpressionSummaryDisplay records
    public static List<FigureExpressionSummaryDisplay> getFigureExpressionSummaryDisplay(List<FigureExpressionSummary> figureExpressionSummaryList) {
        if (figureExpressionSummaryList == null)
            return null;
        List<FigureExpressionSummaryDisplay> list = new ArrayList<FigureExpressionSummaryDisplay>(figureExpressionSummaryList.size());
        for (FigureExpressionSummary summary : figureExpressionSummaryList) {
            for (ExpressedGene expressedGene : summary.getExpressedGenes()) {
                FigureExpressionSummaryDisplay display = new FigureExpressionSummaryDisplay(summary.getFigure());
                display.setExpressedGene(expressedGene);
                if (list.contains(display)) {
                    for (FigureExpressionSummaryDisplay fesd : list) {
                        if (fesd.getFigure().equals(display.getFigure()) && fesd.getExpressedGene().getGene().equals(display.getExpressedGene().getGene())) {
                            // add missing expression statements
                            fesd.getExpressedGene().addExpressionStatements(display.getExpressedGene().getExpressionStatements());
                        }
                    }
                } else
                    list.add(display);
            }
        }
        Collections.sort(list);
        return list;
    }

    public static List<GeneCentricExpressionData> getGeneCentricExpressionData(List<FigureExpressionSummary> figureExpressionSummaryList) {
        if (figureExpressionSummaryList == null)
            return null;
        List<GeneCentricExpressionData> list = new ArrayList<GeneCentricExpressionData>(figureExpressionSummaryList.size());
        Map<Marker, GeneCentricExpressionData> expressedGeneMap = new HashMap<Marker, GeneCentricExpressionData>(figureExpressionSummaryList.size());
        for (FigureExpressionSummary summary : figureExpressionSummaryList) {
            for (ExpressedGene expressedGene : summary.getExpressedGenes()) {
                GeneCentricExpressionData mapData = expressedGeneMap.get(expressedGene.getGene());
                boolean newGene = false;
                if (mapData == null) {
                    mapData = new GeneCentricExpressionData(expressedGene);
                    expressedGeneMap.put(expressedGene.getGene(), mapData);
                    newGene = true;
                }
                if (newGene) {
                    FigureData figureData = new FigureData();
                    figureData.addFigure(summary.getFigure());
                    mapData.setFigureData(figureData);
                    list.add(mapData);
                } else {
                    mapData.getFigureData().addFigure(summary.getFigure());
                }
            }
        }
        Collections.sort(list);
        return list;

    }
}
