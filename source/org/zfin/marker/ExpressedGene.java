package org.zfin.marker;

import org.springframework.util.CollectionUtils;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.FigureData;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;

/**
 * Presentation object that holds
 */
public class ExpressedGene {

    private Marker gene;
    private List<ExpressionStatement> expressionStatements;

    public ExpressedGene(Marker gene) {
        this.gene = gene;
    }

    public Marker getGene() {
        return gene;
    }

    public List<ExpressionStatement> getExpressionStatements() {
        return expressionStatements;
    }

    public void setExpressionStatements(List<ExpressionStatement> expressionStatements) {
        this.expressionStatements = expressionStatements;
    }

    public void addExpressionStatement(ExpressionStatement expressionStatement) {
        if (expressionStatements == null)
            expressionStatements = new ArrayList<ExpressionStatement>();
        if (!expressionStatements.contains(expressionStatement))
            expressionStatements.add(expressionStatement);
    }

    public void addExpressionStatements(List<ExpressionStatement> expressionStatementList) {
        if (CollectionUtils.isEmpty(expressionStatementList))
            return;
        if (expressionStatements == null)
            expressionStatements = new ArrayList<ExpressionStatement>();
        for (ExpressionStatement expressionStatement : expressionStatementList)
            addExpressionStatement(expressionStatement);
    }
}
