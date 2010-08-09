package org.zfin.ontology;

/**
 * COnvenience class to hold matching terms, the match type and the term alias if the match
 * was on an alias.
 */
public class MatchingTerm {

    public final static String OBSOLETE_SUFFIX =" -- OBSOLETED TERM" ;

    private Term term;
    private TermAlias alias; // gives alias, if hit is on alias
    private String query ;

    public MatchingTerm(Term term, String query) {
        this(term,query,null) ;
    }

    public MatchingTerm(Term term, String query , TermAlias alias) {
        this.term = term;
        this.query = query;
        this.alias = alias;
    }

    public Term getTerm() {
        return term;
    }

    public boolean isHitAlias() {
        return (alias!=null && !term.getTermName().toLowerCase().contains(query)) ;
    }


    public boolean startsWithQuery() {
        return (term.getTermName().toLowerCase().startsWith(query)) ;
    }

    public TermAlias getAlias() {
        return alias;
    }

    public String getMatchingTermDisplay() {
        StringBuilder sb = new StringBuilder(20);
        sb.append(term.getTermName());
        if (alias != null) {
            sb.append(" [");
            sb.append(alias.getAlias());
            sb.append("]");
        }
        if (term.isObsolete()) {
            sb.append(OBSOLETE_SUFFIX);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchingTerm matchingTerm = (MatchingTerm) o;
        return matchingTerm.getTerm().equals(getTerm()) ;
    }

    @Override
    public int hashCode() {
        return term != null ? term.hashCode() : 0;
    }

    public boolean isID(){
        return term.getID().equalsIgnoreCase(query);
    }
}
