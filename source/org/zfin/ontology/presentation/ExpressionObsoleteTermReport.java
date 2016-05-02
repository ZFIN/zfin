package org.zfin.ontology.presentation;

import org.zfin.expression.ExpressionResult2;

/**
 * Report for expressions with obsoleted terms.
 */
public class ExpressionObsoleteTermReport extends ObsoleteTermReport {

    private ExpressionResult2 expressionResult;

    public ExpressionObsoleteTermReport(ExpressionResult2 expressionResult) {
        this.expressionResult = expressionResult;
    }

    public ExpressionResult2 getExpressionResult() {
        return expressionResult;
    }
}