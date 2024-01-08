package org.zfin.expression;

import org.junit.Test;
import org.zfin.anatomy.DevelopmentStage;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ExpressionExperimentTest {


    @Test
    public void distinctExpressionsDuplicateFigures() {
        ExpressionFigureStage figureStage = new ExpressionFigureStage();
        ExpressionFigureStage figureStage1 = new ExpressionFigureStage();
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-1");
        figureStage.setStartStage(start);
        figureStage1.setStartStage(start);
        DevelopmentStage end = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-2");
        figureStage.setEndStage(end);
        figureStage1.setEndStage(end);
        Figure figureOne = new FigureFigure();
        figureOne.setZdbID("ZDB-FIG-1");
        Figure figureTwo = new FigureFigure();
        figureTwo.setZdbID("ZDB-FIG-1");
        figureStage.setFigure(figureOne);
        figureStage1.setFigure(figureTwo);

        ExpressionExperiment2 experiment = new ExpressionExperiment2();
        figureStage.setExpressionExperiment(experiment);
        figureStage1.setExpressionExperiment(experiment);
        experiment.setFigureStageSet(Set.of(figureStage, figureStage1));
        assertEquals(1, experiment.getDistinctExpressions());
    }

    @Test
    public void distinctExpressionsTwoFigures() {
        ExpressionFigureStage figureStage = new ExpressionFigureStage();
        ExpressionFigureStage figureStage1 = new ExpressionFigureStage();
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-1");
        figureStage.setStartStage(start);
        figureStage1.setStartStage(start);
        DevelopmentStage end = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-2");
        figureStage.setEndStage(end);
        figureStage1.setEndStage(end);
        Figure figureOne = new FigureFigure();
        figureOne.setZdbID("ZDB-FIG-1");
        Figure figureTwo = new FigureFigure();
        figureTwo.setZdbID("ZDB-FIG-2");
        figureStage.setFigure(figureOne);
        figureStage1.setFigure(figureTwo);
        ExpressionExperiment2 experiment = new ExpressionExperiment2();
        experiment.setFigureStageSet(Set.of(figureStage, figureStage1));
        assertEquals(2, experiment.getDistinctExpressions());
    }

    @Test
    public void distinctExpressionsTwoFiguresTwoExpressions() {
        ExpressionFigureStage figureStage = new ExpressionFigureStage();
        ExpressionFigureStage figureStage1 = new ExpressionFigureStage();
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-1");
        figureStage.setStartStage(start);
        figureStage1.setStartStage(start);
        DevelopmentStage end = new DevelopmentStage();
        end.setZdbID("ZDB-STAGE-2");
        figureStage.setEndStage(end);
        figureStage1.setEndStage(end);
        Figure figureOne = new FigureFigure();
        figureOne.setZdbID("ZDB-FIG-1");
        Figure figureTwo = new FigureFigure();
        figureTwo.setZdbID("ZDB-FIG-2");
        figureStage.setFigure(figureOne);
        figureStage1.setFigure(figureTwo);

        ExpressionFigureStage figureStage2 = new ExpressionFigureStage();
        ExpressionFigureStage figureStage3 = new ExpressionFigureStage();
        DevelopmentStage startOne = new DevelopmentStage();
        startOne.setZdbID("ZDB-STAGE-3");
        figureStage2.setStartStage(startOne);
        figureStage3.setStartStage(startOne);
        DevelopmentStage endOne = new DevelopmentStage();
        endOne.setZdbID("ZDB-STAGE-4");
        figureStage2.setEndStage(endOne);
        figureStage3.setEndStage(endOne);
        Figure figureThree = new FigureFigure();
        figureThree.setZdbID("ZDB-FIG-1");
        Figure figureFour = new FigureFigure();
        figureFour.setZdbID("ZDB-FIG-2");
        figureStage2.setFigure(figureThree);
        figureStage3.setFigure(figureFour);


        ExpressionExperiment2 experiment = new ExpressionExperiment2();
        experiment.setFigureStageSet(Set.of(figureStage, figureStage1,figureStage2,figureStage3));
        assertEquals(4, experiment.getDistinctExpressions());
    }
}