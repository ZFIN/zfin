package org.zfin.marker;

import org.springframework.util.CollectionUtils;
import org.zfin.expression.ExpressionStatement;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Presentation object that holds
 */
public class ExpressedGene {

    private Marker gene;
    private Set<ExpressionStatement> expressionStatements;

    public ExpressedGene(Marker gene) {
        this.gene = gene;
    }

    public Marker getGene() {
        return gene;
    }

    public List<ExpressionStatement> getExpressionStatements() {
        return expressionStatements.stream().collect(Collectors.toList());
    }

    public void addExpressionStatement(ExpressionStatement expressionStatement) {
        if (expressionStatements == null) {
            expressionStatements = new TreeSet<>();
        }
        expressionStatements.add(expressionStatement);
    }

    public void addExpressionStatements(List<ExpressionStatement> expressionStatementList) {
        if (CollectionUtils.isEmpty(expressionStatementList)) {
            return;
        }
        for (ExpressionStatement expressionStatement : expressionStatementList) {
            addExpressionStatement(expressionStatement);
        }
    }
}
