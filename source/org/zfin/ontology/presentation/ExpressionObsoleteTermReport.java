package org.zfin.ontology.presentation;

import org.zfin.expression.ExpressionResult;

/**
 * Report for expressions with obsoleted terms.
 */
public class ExpressionObsoleteTermReport extends ObsoleteTermReport{

    private ExpressionResult expressionResult;

    public ExpressionObsoleteTermReport(ExpressionResult expressionResult) {
        this.expressionResult = expressionResult;
    }

    public ExpressionResult getExpressionResult() {
        return expressionResult;
    }
}