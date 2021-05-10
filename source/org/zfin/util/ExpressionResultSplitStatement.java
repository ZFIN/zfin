package org.zfin.util;

import org.zfin.expression.ExpressionResult;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ExpressionResultSplitStatement {

    private ExpressionResult originalExpressionResult;
    private List<ExpressionResult> expressionResultList = new ArrayList<ExpressionResult>(5);

    public ExpressionResult getOriginalExpressionResult() {
        return originalExpressionResult;
    }

    public void setOriginalExpressionResult(ExpressionResult originalExpressionResult) {
        this.originalExpressionResult = originalExpressionResult;
    }

    public List<ExpressionResult> getExpressionResultList() {
        return expressionResultList;
    }

    public void setExpressionResultList(List<ExpressionResult> expressionResultList) {
        this.expressionResultList = expressionResultList;
    }



    @Override
    public String toString() {
        return "Original: " + originalExpressionResult
                + "lines = " + expressionResultList;
    }
}
