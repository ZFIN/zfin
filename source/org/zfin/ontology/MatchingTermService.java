package org.zfin.ontology;

import org.apache.commons.lang.StringUtils;
import org.zfin.infrastructure.PatriciaTrieMultiMap;
import org.zfin.ontology.presentation.MatchingTermComparator;
import org.zfin.framework.HibernateUtil ;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service class to obtain a list of matching terms for a given search string and
 * ontology.
 */
public class MatchingTermService {

    public static final int MAXIMUM_NUMBER_OF_MATCHES = 25;

    private int maximumNumberOfMatches = MAXIMUM_NUMBER_OF_MATCHES ;
    private OntologyTokenizer tokenizer = new OntologyTokenizer();

    public MatchingTermService() { }

    public MatchingTermService(int maxLimit) {
        maximumNumberOfMatches = maxLimit ;
    }


    protected Set<MatchingTerm> getMatchingTerms(PatriciaTrieMultiMap<Term> termMap,String query){
        Set<MatchingTerm> matchingTermSet = new TreeSet<MatchingTerm>(new MatchingTermComparator(query)) ;
        String[] termsToMatch = query.toLowerCase().trim().split("\\s+") ;
        for(String termToMatch : termsToMatch){
            for(Term term : termMap.getSuggestedValues(termToMatch)){
                // used to support testing only
                // TODO: update when this gets more permanently fixed
                if(term.getZdbID()!=null){
                    HibernateUtil.currentSession().refresh(term) ;
                }

                // if term contains query
                if(containsAllTokens(term.getTermName(),termsToMatch)
//                        ||
//                        !term.isAliasesExist()
                        ){
                    matchingTermSet.add( new MatchingTerm(term,query) ) ;
                }
                else
                if(term.isAliasesExist()){
                    // only add the first matching hit
                    //
                    // if no hits are found?!?? , then just add the last one I guess.


                    TermAlias aliasToView = null ;
                    for(Iterator<TermAlias> termAliasIterator = term.getAliases().iterator() ;
                        termAliasIterator.hasNext() && aliasToView==null ; ){

                        TermAlias alias = termAliasIterator.next();
                        if(containsAllTokens(alias.getAlias().toLowerCase(),termsToMatch)){
                            aliasToView = alias ;
                        }

                    }
                    // if no hit found
                    if(aliasToView==null ){
                        aliasToView = term.getAliases().iterator().next();
                    }

                    if(containsAllTokens(aliasToView.getAlias(),termsToMatch)){
                        matchingTermSet.add( new MatchingTerm(term,query, aliasToView) ) ;
                    }
                }
//                else{
//                    throw new RuntimeException("should never get to this state["+query+"]") ;
//                }
            }
        }
        return matchingTermSet ;
    }

    /**
     * Each hit must match a starting token in the matchTerm.
     * @param matchTerm This is typically one or 2 when split.
     * @param hits  This is typically one or 2.
     * @return
     */
    protected boolean containsAllTokens(String matchTerm, String[] hits) {

        Set<String> matchTokens = tokenizer.tokenize(matchTerm.toLowerCase().trim());
        for(String hit:hits){
            boolean matches = false ;
            for(Iterator<String> iter = matchTokens.iterator() ; iter.hasNext() && false==matches ; ){
                if(iter.next().toLowerCase().startsWith(hit.toLowerCase())){
                    matches = true ;
                }
            }
            if(matches==false){
                return false ;
            }
        }
        return true ;
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
    public Set<MatchingTerm> getMatchingTerms(Ontology ontology, String query) {
        Set<MatchingTerm> matchingTermsForSet = new TreeSet<MatchingTerm>(new MatchingTermComparator(query)) ;
        if (StringUtils.isEmpty(query.trim())){
            return matchingTermsForSet ;
        }

        if(ontology.isComposedOntologies()){
            for(Ontology subOntology : ontology.getIndividualOntologies()){
                matchingTermsForSet.addAll(getMatchingTerms(subOntology,query)) ;
            }
            return matchingTermsForSet ;
        }

        PatriciaTrieMultiMap<Term> termMap = OntologyManager.getInstance().getTermOntologyMap(ontology);
        matchingTermsForSet.addAll(getMatchingTerms(termMap,query)) ;

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

    public int getMaximumNumberOfMatches() {
        return maximumNumberOfMatches;
    }

    public void setMaximumNumberOfMatches(int maximumNumberOfMatches) {
        this.maximumNumberOfMatches = maximumNumberOfMatches;
    }
}
