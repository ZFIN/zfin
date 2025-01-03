package org.zfin.util;

import org.zfin.expression.ExpressionResult2;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ExpressionResultSplitStatement {

    private ExpressionResult2 originalExpressionResult;
    private List<ExpressionResult2> expressionResultList = new ArrayList<>(5);

    public ExpressionResult2 getOriginalExpressionResult() {
        return originalExpressionResult;
    }

    public void setOriginalExpressionResult(ExpressionResult2 originalExpressionResult) {
        this.originalExpressionResult = originalExpressionResult;
    }

    public List<ExpressionResult2> getExpressionResultList() {
        return expressionResultList;
    }

    public void setExpressionResultList(List<ExpressionResult2> expressionResultList) {
        this.expressionResultList = expressionResultList;
    }


    @Override
    public String toString() {
        return "Original: " + originalExpressionResult
               + "lines = " + expressionResultList;
    }
}
