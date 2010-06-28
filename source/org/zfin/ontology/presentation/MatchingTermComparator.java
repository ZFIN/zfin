package org.zfin.ontology.presentation;

import org.zfin.ontology.MatchingTerm;

import java.util.Comparator;

/**
 */
public class MatchingTermComparator implements Comparator<MatchingTerm> {

    private String query ;

    public MatchingTermComparator(String query){
        this.query = query ;
    }

    /**
     * Ordering factors:
     * - direct hit
     * - is alias
     * - is obsolete
     *
     * Within these the next set is:
     * - exact match
     * - match on first word
     * - match on later index
     *
     * Within each one of these, just alphabetize using the AlphaNumeric Comparator
     *
     *
     * @param rhs Term to compare against.
     * @return
     */
    @Override
    public int compare(MatchingTerm lhs, MatchingTerm rhs) {

        if(lhs.getTerm().equals(rhs.getTerm())) return 0 ;

        if(lhs.getTerm().getTermName().equalsIgnoreCase(query) && !lhs.getTerm().isObsolete()){
            return -1  ;
        }

        int rhsCompare = scoreTerm(rhs) ;
        int lhsCompare = scoreTerm(lhs) ;

        if(rhsCompare!=lhsCompare){
            return lhsCompare - rhsCompare;
        }


        return lhs.getTerm().getTermName().toLowerCase().compareTo(rhs.getTerm().getTermName().toLowerCase()) ;
    }

    protected int scoreTerm(MatchingTerm term) {
        int score = 0 ;
        // for each token that hits the first


        if(!term.startsWithQuery()) score += 100 ;
        if(term.isHitAlias()) score += 10000 ;
        if(term.getTerm().isObsolete()) score += 100000 ;

        return score ;
    }
}
