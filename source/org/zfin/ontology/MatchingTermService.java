package org.zfin.ontology;

import java.util.*;

/**
 * Service class to obtain a list of matching terms for a given search string and
 * ontology.
 */
public class MatchingTermService {

    public static final int MAXIMUM_NUMBER_OF_MATCHES = 25;

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
     * Retrieve a list of terms that match a given query in a given ontology.<br/>
     * If the ontology is composed of more than one individual ontologies
     * the matching is performed on the combined, sorted ontology.
     * <p/>
     * First all Start_With matches are retrieved, then Contains matches as that is the default order of the return
     * collection.
     * There is an internal maximum number of returned terms which will truncate the search when this number is exceeded.
     * This may avoid looping over the Contains matches (including the alias matches) which are much slower.
     * <p/>
     * The list is sorted by: <br/>
     * 1) Starts with         <br/>
     * 2) Contains <br/>
     * 3) Obsolete
     *
     * @param ontology Ontology
     * @param query    query string
     * @return list of terms
     */
    public List<MatchingTerm> getMatchingTerms(Ontology ontology, String query) {
        if (query == null)
            return null;

        boolean isMaxNumOfTermsFound = false;
        MatchingTermService service = new MatchingTermService(query, MAXIMUM_NUMBER_OF_MATCHES);
        // check all terms for matches
        Map<String, Term> termMap = OntologyManager.getInstance().getTermOntologyMap(ontology);
        for (String termName : termMap.keySet()) {
            Term term = termMap.get(termName);
            service.compareWithTerm(term);
            isMaxNumOfTermsFound = service.hasMaximumNumberOfTerms();
        }
        // if max number is reached by starts_with matches stop here and return the list
        // as the list always puts the starts_with matches before the contains matches.
        if (isMaxNumOfTermsFound)
            return service.getTermsMatchingStart();

        // search alias map if it exists for matches
        Map<String, List<TermAlias>> aliasMap = OntologyManager.getInstance().getAliasOntologyMap(ontology);
        if (aliasMap != null) {
            for (String termName : aliasMap.keySet()) {
                List<TermAlias> termAliases = aliasMap.get(termName);
                for (TermAlias termAlias : termAliases) {
                    service.compareWithAlias(termAlias);
                }
            }
        }
        // search obsolete map
        Map<String, Term> obsoleteMap = OntologyManager.getInstance().getObsoleteTermMap(ontology);
        if (obsoleteMap != null) {
            for (String termName : obsoleteMap.keySet()) {
                Term obsoleteTerm = obsoleteMap.get(termName);
                service.compareWithObsoleteTerm(obsoleteTerm);
            }
        }
        return service.getCompleteMatchingList();
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
