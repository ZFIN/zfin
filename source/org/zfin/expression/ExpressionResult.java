package org.zfin.expression;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.Term;

import java.util.HashSet;
import java.util.Set;

/**
 * Expression ExpressionExperiment.
 */
public class ExpressionResult {

    private String zdbID;
    private AnatomyItem anatomyTerm;
    private AnatomyItem secondaryAnatomyTerm;
    private boolean expressionFound;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private ExpressionExperiment expressionExperiment;
    private Set<ExpressionResult> expressionResults;
    private Set<Figure> figures;
    protected Term subterm;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public Set<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(Set<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public ExpressionExperiment getExpressionExperiment() {
        return expressionExperiment;
    }

    public void setExpressionExperiment(ExpressionExperiment expressionExperiment) {
        this.expressionExperiment = expressionExperiment;
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public void addFigure(Figure figure){
        if(figures == null)
            figures = new HashSet<Figure>();
        figures.add(figure);
    }

    public AnatomyItem getAnatomyTerm() {
        return anatomyTerm;
    }

    public void setAnatomyTerm(AnatomyItem anatomyTerm) {
        this.anatomyTerm = anatomyTerm;
    }

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }

    public AnatomyItem getSecondaryAnatomyTerm() {
        return secondaryAnatomyTerm;
    }

    public void setSecondaryAnatomyTerm(AnatomyItem secondaryAnatomyTerm) {
        this.secondaryAnatomyTerm = secondaryAnatomyTerm;
    }
    public Term getSubTerm() {
        return subterm;
    }

    public void removeFigure(Figure figure) {
        if(figures != null)
            figures.remove(figure);
    }
}
