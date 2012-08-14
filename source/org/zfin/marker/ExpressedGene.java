package org.zfin.marker;

import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.FigureData;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.publication.Publication;

import java.util.List;

/**
 * Presentation object that holds
 */
public class ExpressedGene{

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
}
