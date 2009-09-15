package org.zfin.expression;

import org.zfin.anatomy.AnatomyItem;

/**
 * An expression structure in which the subterm is an AO term.
 */
public class AnatomyExpressionStructure extends ExpressionStructure {

    private AnatomyItem subterm;

    public AnatomyItem getSubterm() {
        return subterm;
    }

    public void setSubterm(AnatomyItem subterm) {
        this.subterm = subterm;
    }

    public String getSubtermName() {
        return  subterm == null ? null : subterm.getName();
    }

    public String getSubtermID() {
        return subterm == null ? null : subterm.getZdbID();
    }

}
