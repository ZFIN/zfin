/**
 * Stop words are ones that should be ignored because
 * they are too common to be useful when searching.
 *
 * Towards the end, we have added all the words
 * that appear in the header and footers of each ZFIN page.
 */
package org.zfin.uniquery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.Reader;
import java.util.Set;


public class ZfinAnalyzer extends Analyzer {
    private static Set stopWords;

    public static final String[] STOP_WORDS =
            {
                    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                    "also", "an", "and", "another", "any", "are", "as",
                    "at", "be", "been", "being", "but", "by", "came",
                    "can", "come", "could", "did", "do", "does",
                    "each", "else", "for", "from", "get", "got",
                    "has", "had", "he", "have", "her", "here", "him",
                    "himself", "his", "how", "if", "in", "into", "is",
                    "it", "its", "just", "make", "many", "me", "my",
                    "no", "not", "now", "of", "on", "or", "our", "out", "over",
                    "re", "said", "see", "should", "since", "so", "some",
                    "still", "such", "take", "than", "that", "the",
                    "their", "them", "then", "there", "these",
                    "they", "this", "those", "through", "to", "too",
                    "under", "up", "use", "very", "want", "was",
                    "way", "we", "well", "were", "what", "when",
                    "where", "which", "while", "who", "will",
                    "with", "would", "you", "your", "zfin", "fish",
                    "anatomy", "publication", "profile", "lab", "companies",
                    "acc", "email", "home", "mutant", "transgenic", "wild",
                    "type", "gene", "marker", "clone", "expression", "map",
                    "about", "helpful", "hint", "citing", "copyright",
                    "university", "oregon", "eugene", "logo", "design",
                    "name", "names", "symbol", "blast", "data", "zdb", "all",
                    "bp", "aa", "ab", "citation", "mo", "go", "term", "terms",
                    "input", "welcome", "more", "image", "images", "information",
                    "page", "pages", "length", "view", "other", "development",
                    "zebrafish", "book", "protocol", "meeting", "meetings", "job", "jobs",
                    "a", "b", "c", "d", "e", "f", "g", "h", "i",
                    "j", "k", "l", "m", "n", "o", "p", "q", "r",
                    "s", "t", "u", "v", "w", "x", "y", "z"
            };


    public ZfinAnalyzer() {
        this(STOP_WORDS);
    }


    public ZfinAnalyzer(String[] words) {
        stopWords = StopFilter.makeStopSet(words);
    }


    public final TokenStream tokenStream(String fieldName, Reader reader) {
        return new StopFilter(new LowerCaseFilter(new ZfinTokenizer(reader)), stopWords);
    }


    public final TokenStream nonStoppedTokenStream(String fieldName, Reader reader) {
        return new LowerCaseFilter(new ZfinTokenizer(reader));
    }

}
