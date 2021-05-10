package org.zfin.mutant;

import org.zfin.expression.ExpressionStatement;
import org.zfin.marker.Marker;


import java.util.List;

/**
 * Presentation object that holds
 */
public class ExpressedGenotype {

    private Genotype genotype;
    private List<ExpressionStatement> expressionStatements;

    public ExpressedGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public List<ExpressionStatement> getExpressionStatements() {
        return expressionStatements;
    }

    public void setExpressionStatements(List<ExpressionStatement> expressionStatements) {
        this.expressionStatements = expressionStatements;
    }
}
