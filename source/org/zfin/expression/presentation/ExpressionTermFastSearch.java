package org.zfin.expression.presentation;

import org.zfin.expression.ExpressionResult2;
import org.zfin.ontology.GenericTerm;

import java.io.Serializable;
import java.util.Date;

/**
 * Convenience class to hold expression result data with term data
 * super- and subterms flattened out.
 */
public class ExpressionTermFastSearch implements Serializable {

    private long id;
    private ExpressionResult2 expressionResult;
    private GenericTerm term;
    private Date dateCreated;
    private boolean originalAnnotation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExpressionResult2 getExpressionResult() {
        return expressionResult;
    }

    public void setExpressionResult(ExpressionResult2 expressionResult) {
        this.expressionResult = expressionResult;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isOriginalAnnotation() {
        return originalAnnotation;
    }

    public void setOriginalAnnotation(boolean originalAnnotation) {
        this.originalAnnotation = originalAnnotation;
    }
}
