package org.zfin.ontology;

import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.PatriciaTrieMultiMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes the words.
 */
public class OntologyTokenizer {

    private Logger logger = Logger.getLogger(OntologyTokenizer.class);


    //    private final int DEFAULT_MIN_TOKEN_SIZE = 3 ;
    // match 3 alphanumeric
    private final String DEFAULT_STRING_REGEXP = "(\\p{Alnum}{3,})";
    private final String DEFAULT_WORD_REGEXP = "\\s+";
    private final Pattern pattern = Pattern.compile(DEFAULT_STRING_REGEXP);
    private int minLength = -1;

    public OntologyTokenizer() {
    }

    public OntologyTokenizer(int minLength) {
        this.minLength = minLength;
    }


    // this generates words of the form:
    // expression: 'a-b c-d'
    //  tokenizeString: 'a' and 'b' and 'c' and 'd'
    //  tokenizeWords: 'a-b' and 'c-d'
    // where 'a-b' is larger than the minLength
    protected Set<String> tokenizeWords(String string) {
        Set<String> strings = new HashSet<String>(Arrays.asList(string.split(DEFAULT_WORD_REGEXP)));
        Iterator<String> iter = strings.iterator();
        Set<String> returnStrings = new HashSet<String>();
        String word;
        while (iter.hasNext()) {
            word = iter.next();
            if (word.startsWith("(")) {
                word = word.substring(1);
            }
            if (word.endsWith(")")) {
                word = word.substring(0, word.length() - 1);
            }
            if (word.trim().length() >= minLength) {
                returnStrings.add(word);
            }
        }
        return returnStrings;
    }

    protected Set<String> tokenizeStrings(String string) {
        Matcher matcher = pattern.matcher(string);

        Set<String> returnStrings = new HashSet<String>();

        while (matcher.find()) {
            returnStrings.add(matcher.group());
        }

        return returnStrings;
    }

    protected Set<String> tokenize(String string) {
        Set<String> tokens = tokenizeStrings(string);
        tokens.addAll(tokenizeWords(string));
        return tokens;
    }

    public int tokenizeTerm(TermDTO term, PatriciaTrieMultiMap<TermDTO> termMap) {
        String exactTerm = term.getName().toLowerCase();

        int count = 0;

        count += tokenizeTerm(exactTerm, term, termMap);


        // handle aliases
        if (term.getAliases() != null) {
            // handle alias tokens
            for (String alias : term.getAliases()) {
                String aliasTerm = alias.toLowerCase();
                count += tokenizeTerm(aliasTerm, term, termMap);
            }

        }

        return count;

    }

    private int tokenizeTerm(String exactTerm, TermDTO term, PatriciaTrieMultiMap<TermDTO> termMap) {
        int count = 0;

        // handle tokens
        for (String termName : tokenize(exactTerm)) {
            try {
                termMap.put(termName, term);
                ++count;
            } catch (Exception e) {
                logger.error("failed to add tokenized word: [" + termName + "] from [" + exactTerm + "]", e);
            }
        }

        try {
            if (term.getZdbID() != null) {
                // add internal ID and OBO id to map for lookup purposes.
                termMap.put(term.getZdbID(), term);
                ++count;
                termMap.put(term.getOboID(), term);
                ++count;
            }
            termMap.put(exactTerm, term);
            ++count;
        } catch (Exception e) {
            logger.error("failed to add word: [" + exactTerm + "] from term[" + term + "]", e);
        }
        return count;
    }

}

