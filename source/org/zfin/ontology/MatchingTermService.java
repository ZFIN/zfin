package org.zfin.ontology;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.presentation.MatchingTermComparator;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service class to obtain a list of matching terms for a given search string and
 * ontology.
 */
public class MatchingTermService {

    public static final int MAXIMUM_NUMBER_OF_MATCHES = 25;
    private Logger logger = Logger.getLogger(MatchingTermService.class);

    private int maximumNumberOfMatches = MAXIMUM_NUMBER_OF_MATCHES;
    private OntologyTokenizer tokenizer = new OntologyTokenizer();

    public MatchingTermService() {
    }

    public MatchingTermService(int maxLimit) {
        maximumNumberOfMatches = maxLimit;
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
                    // only add the first matching hit
                    //
                    // if no hits are found?!?? , then just add the last one I guess.


                    String aliasToView = null;
                    for (Iterator<String> termAliasIterator = term.getAliases().iterator();
                         termAliasIterator.hasNext() && aliasToView == null;) {
                        String alias = termAliasIterator.next();
                        if (containsAllTokens(alias.toLowerCase(), termsToMatch)) {
                            aliasToView = alias;
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
//                else{
//                    throw new RuntimeException("should never get to this state["+query+"]") ;
//                }
            }
        }
        return matchingTermSet;
    }

    /**
     * Each hit must match a starting token in the matchTerm.
     *
     * @param matchTerm This is typically one or 2 when split.
     * @param hits      This is typically one or 2.
     * @return
     */
    protected boolean containsAllTokens(String matchTerm, String[] hits) {

        Set<String> matchTokens = tokenizer.tokenize(matchTerm.toLowerCase().trim());
        for (String hit : hits) {
            boolean matches = false;
            for (Iterator<String> iter = matchTokens.iterator(); iter.hasNext() && false == matches;) {
                if (iter.next().toLowerCase().startsWith(hit.toLowerCase())) {
                    matches = true;
                }
            }
            if (matches == false) {
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
    public Set<MatchingTerm> getMatchingTerms(String query, Ontology ontology) {
        Set<MatchingTerm> matchingTermsForSet = new TreeSet<MatchingTerm>(new MatchingTermComparator(query));
        if (StringUtils.isEmpty(query.trim())) {
            return matchingTermsForSet;
        }

        if (ontology.isComposedOntologies()) {
            for (Ontology subOntology : ontology.getIndividualOntologies()) {
                matchingTermsForSet.addAll(getMatchingTerms(query, subOntology));
            }
            return matchingTermsForSet;
        }

        for (OntologyDTO ontologyDTO : OntologyManager.getInstance().getOntologies(ontology)) {
            matchingTermsForSet.addAll(getMatchingTerms(query, OntologyManager.getInstance().getTermsForOntology(ontologyDTO)));
        }

        if(maximumNumberOfMatches>0 && matchingTermsForSet.size()>maximumNumberOfMatches){
            Set<MatchingTerm> matchingTerms = new TreeSet<MatchingTerm>(new MatchingTermComparator(query)) ;
            int i = 0 ;
            for(Iterator<MatchingTerm> iterator = matchingTermsForSet.iterator()   ;
                iterator.hasNext() && i < maximumNumberOfMatches ; ){
                matchingTerms.add(iterator.next()) ;
                ++i ;
            }
            return matchingTerms ;
        }
        else{
            return matchingTermsForSet ;
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
