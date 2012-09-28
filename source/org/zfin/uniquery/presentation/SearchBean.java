package org.zfin.uniquery.presentation;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.uniquery.SiteSearchService;
import org.zfin.uniquery.ZfinAnalyzer;
import org.zfin.uniquery.categories.SiteSearchCategories;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The SearchBean is a Bean that is used by the JSP Quick Search tool
 * to perform a large variety of tasks.
 * <p/>
 * Because a user will most likely perform a query, then browse categories, we should
 * cache the results and so this bean should be used in a "Session" mode context so
 * that the results are stored in memory for each user.  As a result, this will
 * take up a significant amount of system memory.
 */
public class SearchBean extends PaginationBean {

    private static final Logger LOG = Logger.getLogger(SearchBean.class);

    // parameters set by web page query
    private String query;
    private String categoryID;

    // ***********************************

    private ReplacementZdbID replacementZdbID;
    private static ZfinAnalyzer analyzer = new ZfinAnalyzer();

    /**
     * A function that returns the "stop words" or common words that should be ignored
     * given a user query.
     *
     * @param queryString query
     * @return List of strings
     */
    private List<String> getIgnoredWords(String queryString) {
        return findStopWords(queryString);
    }


    /**
     * A function that encapsulates the ignored words in HTML formatting for use by the JSP.
     * Ignored words are treated as a separate paragraph, usually one sentence long.
     *
     * @return List of ignored words
     */
    public List<String> getIgnoredWords() {
        return getIgnoredWords(SiteSearchHelper.getQueryTerm(query));
    }

    /**
     * This function supports the ignored words functions.
     *
     * @param queryString query
     * @return List
     */
    private List<String> findStopWords(String queryString) {
        List<String> ignoredWords = new ArrayList<String>();
        TokenStream queryTokenStream = analyzer.tokenStream(SiteSearchService.BODY, new StringReader(queryString));
        TokenStream nonStoppedTokenStream = analyzer.nonStoppedTokenStream(SiteSearchService.BODY, new StringReader(queryString));

        boolean shouldContinue = true;
        try {
            while (shouldContinue) {
                String nextQueryTerm;
                Token nextQueryToken = queryTokenStream.next();
                if (nextQueryToken == null) {
                    nextQueryTerm = null;
                    shouldContinue = false;
                } else {
                    nextQueryTerm = new String(nextQueryToken.termBuffer(), 0, nextQueryToken.termLength());
                }

                Token nextNonStoppedToken;
                while ((nextNonStoppedToken = nonStoppedTokenStream.next()) != null) {
                    String nextNonStoppedTerm;
                    nextNonStoppedTerm = new String(nextNonStoppedToken.termBuffer(), 0, nextNonStoppedToken.termLength());
                    if (nextNonStoppedTerm.equals(nextQueryTerm)) {
                        break;
                    } else {
                        ignoredWords.add(nextNonStoppedTerm);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
        return ignoredWords;
    }

    public String getCategoryID() {
        if (categoryID == null)
            return "ALL";
        return categoryID.trim();
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setReplacementZdbID(ReplacementZdbID replacementZdbID) {
        this.replacementZdbID = replacementZdbID;
    }

    public ReplacementZdbID getReplacementZdbID() {
        return replacementZdbID;
    }

    public String getCategorySearch() {
        String categoryDescription = SiteSearchCategories.getDisplayName(getCategoryID());
        if (categoryDescription.equals("All")) {
            return "Search";
        } else {
            return categoryDescription + " search";
        }
    }

}

