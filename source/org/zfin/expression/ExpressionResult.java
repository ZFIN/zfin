package org.zfin.expression;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Expression ExpressionExperiment.
 */
public class ExpressionResult implements Comparable<ExpressionResult> {

    private String zdbID;
    private Term superterm;
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

    public void addFigure(Figure figure) {
        if (figures == null)
            figures = new HashSet<Figure>();
        figures.add(figure);
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

    public Term getSubterm() {
        return subterm;
    }

    public Term getSuperterm() {
        return superterm;
    }

    public void setSuperterm(Term superterm) {
        this.superterm = superterm;
    }

    public void setSubterm(Term subterm) {
        this.subterm = subterm;
    }

    public void removeFigure(Figure figure) {
        if (figures != null)
            figures.remove(figure);
    }

    /**
     * Not sure if this is significantly better than a figure, here.
     *
     * @param figure Figure to match
     * @return Matched figure or null if not found.
     */
    public Figure getMatchingFigure(Figure figure) {
        if (CollectionUtils.isNotEmpty(getFigures())) {
            for (Figure aFigure : getFigures()) {
                if (aFigure.equals(figure)) {
                    return aFigure;
                }
            }
        }
        return null;
    }

    @Override
    public int compareTo(ExpressionResult o) {
        Marker gene = getExpressionExperiment().getGene();

        String nameOne;
        if (gene != null)
            nameOne = gene.getAbbreviation();
        else
            nameOne = getExpressionExperiment().getAntibody().getName();
        String nameTwo;
        Marker geneTwo = o.getExpressionExperiment().getGene();
        if (geneTwo != null)
            nameTwo = geneTwo.getAbbreviation();
        else
            nameTwo = o.getExpressionExperiment().getAntibody().getName();
        if (!nameOne.equals(nameTwo))
            return nameOne.compareTo(nameTwo);
        return 0;
    }
}
