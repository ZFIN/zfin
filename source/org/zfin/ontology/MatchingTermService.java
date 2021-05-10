package org.zfin.ontology;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.PatriciaTrieMultiMap;

import java.util.*;

/**
 * Service class to obtain a list of matching terms for a given search string and
 * ontology.
 */
public class MatchingTermService {

    public static final int MAXIMUM_NUMBER_OF_MATCHES = 25;
    private Logger logger = LogManager.getLogger(MatchingTermService.class);

    private int maximumNumberOfMatches = MAXIMUM_NUMBER_OF_MATCHES;
    private OntologyTokenizer tokenizer = new OntologyTokenizer();

    public MatchingTermService() {
    }

    // a negative number indicates all matches should be returned
    public MatchingTermService(int maxLimit) {
        if (maxLimit < 0)
            maximumNumberOfMatches = Integer.MAX_VALUE;
        else
            maximumNumberOfMatches = maxLimit;
    }

    public List<TermDTO> getMatchingTermList(String query, Ontology ontology) {
        List<TermDTO> termDTOList = new ArrayList<TermDTO>(0);

        List<MatchingTerm> matchingTerms = getMatchingTerms(query, ontology);
        if (matchingTerms == null)
            return termDTOList;

        termDTOList = new ArrayList<TermDTO>(matchingTerms.size());
        for (MatchingTerm matchingTerm : matchingTerms) {
            termDTOList.add(matchingTerm.getTerm());
        }
        return termDTOList;
    }


    protected Set<MatchingTerm> getMatchingTerms(String query, PatriciaTrieMultiMap<TermDTO> termMap) {
        Set<MatchingTerm> matchingTermSet = new TreeSet<MatchingTerm>(new MatchingTermComparator(query));
        String[] termsToMatch = query.toLowerCase().trim().split("\\s+");
        for (String termToMatch : termsToMatch) {
            Set<TermDTO> matchedTerms = termMap.getSuggestedValues(termToMatch);
            for (TermDTO term : matchedTerms) {

                // if term contains query
                if (containsAllTokens(term.getName(), termsToMatch)
//                        ||
//                        !term.isAliasesExist()
                        ) {
                    matchingTermSet.add(new MatchingTerm(term, query));
                } else if (term.isAliasesExist()) {
                    // add the best matching alias (levenshtein distance)
                    // if no hits are found add the last one I guess.

                    String aliasToView = null;
                    int levenshteinDistance = 10000;
                    for (String alias : term.getAliases()) {
                        if (containsAllTokens(alias.toLowerCase(), termsToMatch)) {
                            int distance = StringUtils.getLevenshteinDistance(alias.toLowerCase(), query.toLowerCase());
                            if (distance < levenshteinDistance) {
                                aliasToView = alias;
                                levenshteinDistance = distance;
                            }
                        }
                    }
                    // if no hit found
                    if (aliasToView == null && term.getAliases() != null) {
                        aliasToView = term.getAliases().iterator().next();
                    }

                    if (aliasToView == null) {
                        logger.error("Alias has no name: " + aliasToView + " for term : " + term + " query: " + query);
                    } else if (containsAllTokens(aliasToView, termsToMatch)) {
                        matchingTermSet.add(new MatchingTerm(term, query, aliasToView));
                    }
                }
            }
        }
        return matchingTermSet;
    }

    /**
     * Each hit must match a starting token in the matchTerm.
     *
     * @param matchTerm This is typically one or 2 when split.
     * @param hits      This is typically one or 2.
     * @return boolean if it is a match
     */
    protected boolean containsAllTokens(String matchTerm, String[] hits) {

        Set<String> matchTokens = tokenizer.tokenize(matchTerm.toLowerCase().trim());
        for (String hit : hits) {
            boolean matches = false;
            for (Iterator<String> iterator = matchTokens.iterator(); iterator.hasNext() && !matches; ) {
                if (iterator.next().toLowerCase().startsWith(hit.toLowerCase())) {
                    matches = true;
                }
            }
            if (!matches) {
                return false;
            }
        }
        return true;
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
     * @param query    query string
     * @param ontology Ontology
     * @return list of terms
     */
    public List<MatchingTerm> getMatchingTerms(String query, Ontology ontology) {
        List<MatchingTerm> matchingTermsForSet = new ArrayList<MatchingTerm>();
        if (StringUtils.isEmpty(query.trim())) {
            return matchingTermsForSet;
        }

        if (ontology.isComposedOntologies()) {
            for (Ontology subOntology : ontology.getIndividualOntologies()) {
                matchingTermsForSet.addAll(getMatchingTerms(query, subOntology));
            }
        } else {
            for (OntologyDTO ontologyDTO : OntologyManager.getInstance().getOntologies(ontology)) {
                matchingTermsForSet.addAll(getMatchingTerms(query, OntologyManager.getInstance().getTermsForOntology(ontologyDTO)));
            }
        }
        Collections.sort(matchingTermsForSet, new MatchingTermComparator(query));
        if (maximumNumberOfMatches > 0 && matchingTermsForSet.size() > maximumNumberOfMatches) {
            List<MatchingTerm> matchingTerms = new ArrayList<MatchingTerm>();
            int i = 0;
            for (Iterator<MatchingTerm> iterator = matchingTermsForSet.iterator();
                 iterator.hasNext() && i < maximumNumberOfMatches; ) {
                matchingTerms.add(iterator.next());
                ++i;
            }
            return matchingTerms;
        } else {
            return matchingTermsForSet;
        }
    }

    /**
     * Removes a given Term ID from set.
     * Needed to suppress 'normal' and 'abnormal' from the collection.
     *
     * @param termID              the term to be removed from set
     * @param matchingTermsForSet set to be modified
     */
    private void removeTermsFromSet(String termID, Set<MatchingTerm> matchingTermsForSet) {
        if (termID == null || matchingTermsForSet == null)
            return;

        for (MatchingTerm term : matchingTermsForSet) {
            if (term.getTerm().getZdbID().equals(termID)) {
                matchingTermsForSet.remove(term);
                break;
            }
        }
    }

    public int getMaximumNumberOfMatches() {
        return maximumNumberOfMatches;
    }

    public void setMaximumNumberOfMatches(int maximumNumberOfMatches) {
        this.maximumNumberOfMatches = maximumNumberOfMatches;
    }
}
