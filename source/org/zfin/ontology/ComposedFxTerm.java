package org.zfin.ontology;

import org.zfin.anatomy.AnatomyItem;

/**
 * A convenience class to hold a composed term for FX curation,
 * which means:
 * 1) Superterm = Anatomy
 * Subterm = Anatomy or Goterm
 */
public class ComposedFxTerm implements Comparable<ComposedFxTerm> {

    private AnatomyItem superTerm;
    private OntologyTerm subterm;
    private boolean expressionFound;

    public AnatomyItem getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(AnatomyItem superTerm) {
        this.superTerm = superTerm;
    }

    public OntologyTerm getSubterm() {
        return subterm;
    }

    public void setSubterm(OntologyTerm subterm) {
        this.subterm = subterm;
    }

    public String getComposedTermName() {
        String name = superTerm.getName();
        if (getSubterm() != null) {
            name += ":";
            name += getSubterm().getTermName();
        }
        return name;
    }

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public int compareTo(ComposedFxTerm term) {
        if (term == null)
            return 1;
        String supertermName = superTerm.getName();
        if (subterm == null && term.getSubterm() != null)
            return -1;
        if (term.getSubterm() == null)
            return +1;
        if (subterm == null && term.getSubterm() == null)
             return supertermName.compareTo(term.getSuperTerm().getName());
        if (!supertermName.equals(term.getSuperTerm().getName()))
            return supertermName.compareTo(term.getSuperTerm().getName());
        else
            return subterm.getTermName().compareTo(term.getSubterm().getTermName());
    }
}
