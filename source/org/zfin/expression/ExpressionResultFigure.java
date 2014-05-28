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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionResultFigure that = (ExpressionResultFigure) o;

        if (expressionResult != null ? !expressionResult.equals(that.expressionResult) : that.expressionResult != null)
            return false;
        if (figure != null ? !figure.equals(that.figure) : that.figure != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = figure != null ? figure.hashCode() : 0;
        result = 31 * result + (expressionResult != null ? expressionResult.hashCode() : 0);
        return result;
    }
}
