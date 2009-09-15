package org.zfin.expression;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.ontology.GoTerm;

/**
 * An expression structure in which the subterm is an AO term.
 */
public class GOExpressionStructure extends ExpressionStructure {

    private GoTerm subterm;

    public GoTerm getSubterm() {
        return subterm;
    }

    public void setSubterm(GoTerm subterm) {
        this.subterm = subterm;
    }

    public String getSubtermName() {
        return subterm.getName();
    }

    public String getSubtermID() {
        return subterm.getZdbID();
    }
}