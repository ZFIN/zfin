package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionResult;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Presentation class
 */
public class ExpressionResultDisplay {

    private Term superterm;
    private List<ExpressionResult> expressionResultList = new ArrayList<ExpressionResult>();
    private DevelopmentStage start;
    private DevelopmentStage end;


    public ExpressionResultDisplay(ExpressionResult expressionResult) {
        this.superterm = expressionResult.getSuperTerm();
        this.start = expressionResult.getStartStage();
        this.end = expressionResult.getEndStage();
        expressionResultList.add(expressionResult);
    }

    public ExpressionResultDisplay(Term superterm, DevelopmentStage start, DevelopmentStage end) {
        this.superterm = superterm;
        this.start = start;
        this.end = end;
    }

    public Term getSuperterm() {
        return superterm;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void addExpressionResult(ExpressionResult expressionResult) {
        expressionResultList.add(expressionResult);
    }

    public List<ExpressionResult> getExpressionResultList() {
        return expressionResultList;
    }

    public String getUniqueKey() {
        return superterm.getOboID()+"|"+start.getOboID()+"|"+end.getOboID();
    }

    public Set<Publication> getDistinctPublications(){
        Set<Publication> publicationSet = new TreeSet<Publication>();
        for (ExpressionResult result : expressionResultList)
            publicationSet.add(result.getExpressionExperiment().getPublication());
        return publicationSet;
    }
}
