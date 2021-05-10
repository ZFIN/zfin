package org.zfin.expression.presentation;

import java.util.List;

/**
 */
public class WildTypeExpression {

    private List<ExpressedStructurePresentation> expressedStructures;
    private StageExpressionPresentation expressionPresentation;

    public List<ExpressedStructurePresentation> getExpressedStructures() {
        return expressedStructures;
    }

    public void setExpressedStructures(List<ExpressedStructurePresentation> expressedStructures) {
        this.expressedStructures = expressedStructures;
    }

    public StageExpressionPresentation getExpressionPresentation() {
        return expressionPresentation;
    }

    public void setExpressionPresentation(StageExpressionPresentation expressionPresentation) {
        this.expressionPresentation = expressionPresentation;
    }
}
