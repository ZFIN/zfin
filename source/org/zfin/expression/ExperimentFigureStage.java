package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.ComposedFxTerm;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Domain object that does not map to a database table directly.
 * It is a unique combination of Experiment, Figure, start and end stage.
 */
public class ExperimentFigureStage {

    private ExpressionExperiment expressionExperiment;
    private Figure figure;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private Set<ExpressionResult> expressionResults;
    //cached variable
    private List<ComposedFxTerm> terms;

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        if (this.start != null && start != null && !this.start.equals(start))
            throw new RuntimeException("The start state has to be unique!");
        this.start = start;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        if (this.end != null && end != null && !this.end.equals(end))
            throw new RuntimeException("The end state has to be unique!");
        this.end = end;
    }

    public ExpressionExperiment getExpressionExperiment() {
        return expressionExperiment;
    }

    public void setExpressionExperiment(ExpressionExperiment expressionExperiment) {
        this.expressionExperiment = expressionExperiment;
    }

    public Set<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(Set<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public List<ComposedFxTerm> getComposedTerms() {
        if (terms != null)
            return terms;
        terms = new ArrayList<ComposedFxTerm>();
        for (ExpressionResult result : expressionResults) {
            ComposedFxTerm term = new ComposedFxTerm();
            term.setSuperTerm(result.getAnatomyTerm());
            term.setSubterm(result.getSubTerm());
            term.setExpressionFound(result.isExpressionFound());
            terms.add(term);
            setStart(result.getStartStage());
            setEnd(result.getEndStage());
        }
        return terms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentFigureStage that = (ExperimentFigureStage) o;

        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (expressionExperiment != null ? !expressionExperiment.getZdbID().equals(that.expressionExperiment.getZdbID()) : that.expressionExperiment != null)
            return false;
        if (figure != null ? !figure.getZdbID().equals(that.figure.getZdbID()) : that.figure != null) return false;
        return !(start != null ? !start.equals(that.start) : that.start != null);

    }

    @Override
    public int hashCode() {
        int result = expressionExperiment != null ? expressionExperiment.hashCode() : 0;
        result = 31 * result + (figure != null ? figure.getZdbID().hashCode() : 0);
        result = 31 * result + (start != null ? start.getZdbID().hashCode() : 0);
        result = 31 * result + (end != null ? end.getZdbID().hashCode() : 0);
        return result;
    }

    public void addExpressionResult(ExpressionResult result) {
        if (expressionResults == null)
            expressionResults = new HashSet<ExpressionResult>();
        expressionResults.add(result);
        start = result.getStartStage();
        end = result.getEndStage();
    }
}
