package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.PatriciaTrieMultiMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes the words.
 */
public class OntologyTokenizer {

    private Logger logger = Logger.getLogger(OntologyTokenizer.class) ;


//    private final int DEFAULT_MIN_TOKEN_SIZE = 3 ;
    // match 3 alphanumeric
    private final String DEFAULT_STRING_REGEXP = "(\\p{Alnum}{3,})" ;
    private final String DEFAULT_WORD_REGEXP = "\\s+" ;
    private final Pattern pattern = Pattern.compile(DEFAULT_STRING_REGEXP) ;


    protected Set<String> tokenizeWords(String string){
        return new HashSet<String>(Arrays.asList(string.split(DEFAULT_WORD_REGEXP)));
    }

    protected Set<String> tokenizeStrings(String string){
        Matcher matcher = pattern.matcher(string) ;

        Set<String> returnStrings = new HashSet<String>() ;

        while(matcher.find()){
            returnStrings.add(matcher.group()) ;
        }

        return returnStrings ;
    }

    protected Set<String> tokenize(String string){
        Set<String> tokens = tokenizeStrings(string) ;
        tokens.addAll(tokenizeWords(string)) ;
        return tokens ;
    }

    public void tokenizeTerm(Term term, PatriciaTrieMultiMap<Term> termMap) {
        String exactTerm = term.getTermName().toLowerCase() ;

        tokenizeTerm(exactTerm,term,termMap) ;


        // handle aliases
        if(term.getAliases()!=null){
            // handle alias tokens
            for(TermAlias alias : term.getAliases()){
                String aliasTerm = alias.getAlias().toLowerCase()  ;
                tokenizeTerm(aliasTerm,alias.getTerm(),termMap);
            }

        }


    }

    private void tokenizeTerm(String exactTerm, Term term, PatriciaTrieMultiMap<Term> termMap) {

        // handle tokens
        for(String termName : tokenize(exactTerm)){
            try {
                termMap.put(termName,term);
            } catch (Exception e) {
                logger.error("failed to add tokenized word: ["+termName+"] from ["+exactTerm+"]",e);
            }
        }
    }

}

