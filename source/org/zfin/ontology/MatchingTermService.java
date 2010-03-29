package org.zfin.ontology;

import java.util.*;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class MatchingTermService {

    private List<MatchingTerm> termsMatchingStart = new ArrayList<MatchingTerm>(10);
    private Collection<MatchingTerm> termsMatchingContains = new ArrayList<MatchingTerm>(10);
    private Collection<MatchingTerm> termsMatchingAlias = new ArrayList<MatchingTerm>(10);
    private Collection<MatchingTerm> termsMatchingObsolete = new ArrayList<MatchingTerm>(10);

    private String queryString;
    private String queryStringLowerCase;
    private int maximumNumberOfMatches;

    public MatchingTermService(String queryString) {
        this.queryString = queryString;
        queryStringLowerCase = queryString.toLowerCase();
    }

    /**
     * Provide a query string and the number of maximum matches that this search
     * should be limited to. The number is taken on 'starts with' matches.
     *
     * @param queryString            query string
     * @param maximumNumberOfMatches maximum number of results per search .
     */
    public MatchingTermService(String queryString, int maximumNumberOfMatches) {
        this.queryString = queryString;
        queryStringLowerCase = queryString.toLowerCase();
        this.maximumNumberOfMatches = maximumNumberOfMatches;
    }

    public void compareWithTerm(Term comparisonTerm) {
        if (comparisonTerm == null)
            return;

        String comparisonTermName = comparisonTerm.getTermName().toLowerCase();
        if (comparisonTermName.startsWith(queryStringLowerCase)) {
            MatchingTerm matchTerm = new MatchingTerm(comparisonTerm, true);
            termsMatchingStart.add(matchTerm);
        } else if (comparisonTermName.contains(queryStringLowerCase)) {
            MatchingTerm matchTerm = new MatchingTerm(comparisonTerm, false);
            if (!termsMatchingAlias.contains(matchTerm))
                termsMatchingContains.add(matchTerm);
        }
    }

    public void compareWithAlias(TermAlias comparisonTerm) {
        if (comparisonTerm == null)
            return;

        String comparisonTermName = comparisonTerm.getAlias().toLowerCase();
        if (comparisonTermName.startsWith(queryStringLowerCase)) {
            MatchingTerm matchTerm = new MatchingTerm(comparisonTerm.getTerm(), true, comparisonTerm);
            if (!(termsMatchingContains.contains(matchTerm)) && !(termsMatchingStart.contains(matchTerm)))
                termsMatchingAlias.add(matchTerm);
        } else if (comparisonTermName.contains(queryStringLowerCase)) {
            MatchingTerm matchTerm = new MatchingTerm(comparisonTerm.getTerm(), false, comparisonTerm);
            if (!(termsMatchingContains.contains(matchTerm)) && !(termsMatchingStart.contains(matchTerm)))
                termsMatchingAlias.add(matchTerm);
        }
    }

    public void compareWithObsoleteTerm(Term comparisonTerm) {
        if (comparisonTerm == null)
            return;

        String comparisonTermName = comparisonTerm.getTermName().toLowerCase();
        if (comparisonTermName.startsWith(queryStringLowerCase)) {
            MatchingTerm matchTerm = new MatchingTerm(comparisonTerm, true);
            termsMatchingObsolete.add(matchTerm);
        } else if (comparisonTermName.contains(queryStringLowerCase)) {
            MatchingTerm matchTerm = new MatchingTerm(comparisonTerm, false);
            if (!termsMatchingObsolete.contains(matchTerm))
                termsMatchingObsolete.add(matchTerm);
        }
    }

    /**
     * The number of currently found matches on the beginning of the query string.
     * This number could be used to stop the search before the whole ontology is search through.
     *
     * @return number of matches
     */
    public int numberOfStartingMatches() {
        return termsMatchingStart.size();
    }

    public boolean hasMaximumNumberOfTerms() {
        return termsMatchingStart.size() >= maximumNumberOfMatches;
    }

    public List<MatchingTerm> getTermsMatchingStart() {
        return termsMatchingStart;
    }

    public List<MatchingTerm> getTermsMatchingContains() {
        List<MatchingTerm> matches = new ArrayList<MatchingTerm>(termsMatchingContains.size() + termsMatchingAlias.size());
        matches.addAll(termsMatchingContains);
        matches.addAll(termsMatchingAlias);
        Collections.sort(matches, new TermNameComparator());
        return matches;
    }

    public List<MatchingTerm> getCompleteMatchingList() {
        List<MatchingTerm> matches = new ArrayList<MatchingTerm>(termsMatchingStart.size() + termsMatchingContains.size() + termsMatchingAlias.size());
        matches.addAll(termsMatchingStart);
        matches.addAll(getTermsMatchingContains());
        matches.addAll(termsMatchingObsolete);
        return matches;
    }

    private class TermNameComparator implements Comparator<MatchingTerm> {

        public int compare(MatchingTerm o1, MatchingTerm o2) {
            return o1.getTerm().getTermName().compareToIgnoreCase(o2.getTerm().getTermName());
        }
    }

}
