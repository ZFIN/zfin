package org.zfin.expression;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;
import org.zfin.anatomy.DevelopmentStage;

import java.util.HashSet;
import java.util.Set;

public class ExpressionExperimentTest {


    @Test
    public void distinctExpressionsDuplicateFigures() {
        ExpressionResult resultOne = new ExpressionResult();
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-1");
        resultOne.setStartStage(start);
        DevelopmentStage end = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-2");
        resultOne.setEndStage(end);
        Figure figureOne = new FigureFigure();
        figureOne.setZdbID("ZDB-FIG-1");
        Figure figureTwo = new FigureFigure();
        figureTwo.setZdbID("ZDB-FIG-1");
        Set<Figure> figures = new HashSet<Figure>();
        figures.add(figureOne);
        figures.add(figureTwo);
        resultOne.setFigures(figures);

        Set<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        assertEquals(1, experiment.getDistinctExpressions());
    }

    @Test
    public void distinctExpressionsTwoFigures() {
        ExpressionResult resultOne = new ExpressionResult();
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-1");
        resultOne.setStartStage(start);
        DevelopmentStage end = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-2");
        resultOne.setEndStage(end);
        Figure figureOne = new FigureFigure();
        figureOne.setZdbID("ZDB-FIG-1");
        Figure figureTwo = new FigureFigure();
        figureTwo.setZdbID("ZDB-FIG-2");
        Set<Figure> figures = new HashSet<Figure>();
        figures.add(figureOne);
        figures.add(figureTwo);
        resultOne.setFigures(figures);

        Set<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        assertEquals(2, experiment.getDistinctExpressions());
    }

    @Test
    public void distinctExpressionsTwoFiguresTwoExpressions() {
        ExpressionResult resultOne = new ExpressionResult();
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-1");
        resultOne.setStartStage(start);
        DevelopmentStage end = new DevelopmentStage();
        end.setZdbID("ZDB-STAGE-2");
        resultOne.setEndStage(end);
        Figure figureOne = new FigureFigure();
        figureOne.setZdbID("ZDB-FIG-1");
        Figure figureTwo = new FigureFigure();
        figureTwo.setZdbID("ZDB-FIG-2");
        Set<Figure> figures = new HashSet<Figure>();
        figures.add(figureOne);
        figures.add(figureTwo);
        resultOne.setFigures(figures);

        ExpressionResult resultTwo = new ExpressionResult();
        DevelopmentStage startOne = new DevelopmentStage();
        startOne.setZdbID("ZDB-STAGE-3");
        resultTwo.setStartStage(startOne);
        DevelopmentStage endOne = new DevelopmentStage();
        endOne.setZdbID("ZDB-STAGE-4");
        resultTwo.setEndStage(endOne);
        Figure figureThree = new FigureFigure();
        figureThree.setZdbID("ZDB-FIG-1");
        Figure figureFour = new FigureFigure();
        figureFour.setZdbID("ZDB-FIG-2");
        Set<Figure> figuresTwo = new HashSet<Figure>();
        figuresTwo.add(figureThree);
        figuresTwo.add(figureFour);
        resultTwo.setFigures(figuresTwo);

        Set<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        assertEquals(4, experiment.getDistinctExpressions());
    }
}