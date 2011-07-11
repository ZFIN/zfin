package org.zfin.expression.presentation;

import org.zfin.expression.repository.StageExpressionPresentation;

import java.util.List;

/**
 */
public class WildTypeExpression {

    private List<ExpressionExperimentPresentation> expressedStructures;
    private StageExpressionPresentation expressionPresentation;

    public List<ExpressionExperimentPresentation> getExpressedStructures() {
        return expressedStructures;
    }

    public void setExpressedStructures(List<ExpressionExperimentPresentation> expressedStructures) {
        this.expressedStructures = expressedStructures;
    }

    public StageExpressionPresentation getExpressionPresentation() {
        return expressionPresentation;
    }

    public void setExpressionPresentation(StageExpressionPresentation expressionPresentation) {
        this.expressionPresentation = expressionPresentation;
    }
}
