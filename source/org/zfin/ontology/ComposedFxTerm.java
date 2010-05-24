package org.zfin.ontology;

/**
 * A convenience class to hold a composed term for FX curation,
 * which means:
 * 1) Superterm = Anatomy
 * Subterm = Anatomy or Goterm
 */
public class ComposedFxTerm implements Comparable<ComposedFxTerm> {

    private String zdbID;
    private Term superTerm;
    private Term subterm;
    private boolean expressionFound;

    public Term getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(Term superTerm) {
        this.superTerm = superTerm;
    }

    public Term getSubterm() {
        return subterm;
    }

    public void setSubterm(Term subterm) {
        this.subterm = subterm;
    }

    public String getComposedTermName() {
        String name = superTerm.getTermName();
        if (getSubterm() != null) {
            name += ":";
            name += getSubterm().getTermName();
        }
        return name;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
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
        String supertermName = superTerm.getTermName();
        if (subterm == null && term.getSubterm() != null)
            return -1;
        if (term.getSubterm() == null)
            return +1;
        if (subterm == null && term.getSubterm() == null)
             return supertermName.compareTo(term.getSuperTerm().getTermName());
        if (!supertermName.equals(term.getSuperTerm().getTermName()))
            return supertermName.compareTo(term.getSuperTerm().getTermName());
        else
            return subterm.getTermName().compareTo(term.getSubterm().getTermName());
    }
}
