package org.zfin.ontology;

import org.zfin.gwt.root.dto.TermDTO;

/**
 * COnvenience class to hold matching terms, the match type and the term alias if the match
 * was on an alias.
 */
public class MatchingTerm {

    public final static String OBSOLETE_SUFFIX = " -- OBSOLETED TERM";

    private TermDTO term;
    private String alias; // gives alias, if hit is on alias
    private String query;

    public MatchingTerm(TermDTO term, String query) {
        this(term, query, null);
    }

    public MatchingTerm(TermDTO term, String query, String alias) {
        if(term instanceof TermDTO){
            this.term =  term;
        }
        else{
            this.term = term;
        }
        this.query = query;
        this.alias = alias;
    }

    public TermDTO getTerm() {
        return term;
    }

    public boolean isHitAlias() {
        return (alias != null && !term.getName().toLowerCase().contains(query));
    }


    public boolean startsWithQuery() {
        return (term.getName().toLowerCase().startsWith(query));
    }

    public String getAlias() {
        return alias;
    }

    public String getMatchingTermDisplay() {
        StringBuilder sb = new StringBuilder(20);
        sb.append(term.getName());
        if (alias != null) {
            sb.append(" [");
            sb.append(alias);
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
        return matchingTerm.getTerm().equals(getTerm());
    }

    @Override
    public int hashCode() {
        return term != null ? term.hashCode() : 0;
    }

    public boolean isID() {
        return term.getZdbID().equalsIgnoreCase(query);
    }
}
