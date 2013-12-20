package org.zfin.expression;

import java.io.Serializable;

/**
 * Convenience class to allow access to the join table for figure and expression_result table.
 */
public class ExpressionResultFigure implements Serializable {
    private Figure figure;
    private ExpressionResult expressionResult;

    public ExpressionResult getExpressionResult() {
        return expressionResult;
    }

    public void setExpressionResult(ExpressionResult expressionResult) {
        this.expressionResult = expressionResult;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }
}
