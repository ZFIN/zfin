package org.zfin.ontology;

/**
 * COnvenience class to hold matching terms, the match type and the term alias if the match
 * was on an alias.
 */
public class MatchingTerm {

    private Term term;
    private boolean matchingStart;
    private TermAlias alias;

    public MatchingTerm(Term term, boolean isMatchingStart) {
        this.term = term;
        this.matchingStart = isMatchingStart;
    }

    public MatchingTerm(Term term, boolean isMatchingStart, TermAlias alias) {
        this.term = term;
        this.matchingStart = isMatchingStart;
        this.alias = alias;
    }

    public Term getTerm() {
        return term;
    }

    public TermAlias getAlias() {
        return alias;
    }

    public boolean isMatchingStart() {
        return matchingStart;
    }

    public String getMatchingTermDisplay() {
        StringBuilder sb = new StringBuilder(20);
        sb.append(term.getTermName());
        if (alias != null) {
            sb.append(" [");
            sb.append(alias.getAlias());
            sb.append("]");
        }
        if(term.isObsolete()){
            sb.append(" -- OBSOLETED TERM");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchingTerm matchingTerm = (MatchingTerm) o;

        if (term != null ? !term.equals(matchingTerm.term) : matchingTerm.term != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return term != null ? term.hashCode() : 0;
    }
}
