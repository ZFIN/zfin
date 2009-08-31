package org.zfin.expression;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.ontology.OntologyTerm;

/**
 * This expression record defines a postcomposed term with an anatomy term
 * as the subterm.s
 */
public class AnatomyExpressionResult extends ExpressionResult {

    public AnatomyItem getSubterm() {
        return (AnatomyItem) subterm;
    }

    public void setSubterm(AnatomyItem subterm) {
        this.subterm = subterm;
    }

}
