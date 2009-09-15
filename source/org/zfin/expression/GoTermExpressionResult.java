package org.zfin.expression;

import org.zfin.ontology.GoTerm;

/**
 * This expression record defines a postcomposed term with an anatomy term
 * as the subterm.s
 */
public class GoTermExpressionResult extends ExpressionResult {

    public GoTerm getSubterm() {
        return (GoTerm) subterm;
    }

    public void setSubterm(GoTerm subterm) {
        this.subterm = subterm;
    }
}