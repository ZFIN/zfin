package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.FishExperiment;
import org.zfin.publication.Publication;
import org.zfin.sequence.MarkerDBLink;

import java.util.HashSet;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
public class ExpressionFigureStage {
    private long id;
    private ExpressionExperiment2 expressionExperiment;
    private Figure figure;
    private DevelopmentStage start;
    private DevelopmentStage end;

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }

    public ExpressionExperiment2 getExpressionExperiment() {
        return expressionExperiment;
    }

    public void setExpressionExperiment(ExpressionExperiment2 expressionExperiment) {
        this.expressionExperiment = expressionExperiment;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        this.start = start;
    }
}

