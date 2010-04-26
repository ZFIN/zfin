package org.zfin.uniquery;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.zfin.properties.ZfinProperties;
import org.zfin.uniquery.categories.SiteSearchCategories;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * Service class to provided services to site search related problems.
 */
public class SiteSearchService {

    public static final String BODY = "body";

    private static final Logger LOG = Logger.getLogger(SiteSearchService.class);
    private static ZfinAnalyzer analyzer = new ZfinAnalyzer();
    private static String indexDirectory = ZfinProperties.getIndexDirectory();

    /**
     * Check if a given query string produces at least one hit, i.e. at least one page
     * has a reference.
     * @param queryString query string
     * @return true or false
     */
    public static boolean hasHits(String queryString) {
        List<SearchCategory> categories = SiteSearchCategories.allCategories;
        for (SearchCategory category : categories) {
            Hits hits = getHits(category, queryString);
            if (hits != null && hits.length() > 0)
                return true;
        }
        return false;
    }
    /**
     * Retrieve hits for a given category and a given query string.
     * Any error while reading the indexer file will return null and log the exception.
     *
     * @param category    search category
     * @param queryString search string
     * @return Hits object
     */
    public static Hits getHits(SearchCategory category, String queryString) {
        Hits hits = null;
        try {
            Query query = parseQuery(queryString, BODY, new ZfinAnalyzer());
            Query fullQuery = addCategoryPrefixToQuery(category, query, analyzer);
            IndexReader reader = IndexReader.open(indexDirectory);
            Searcher searcher = new IndexSearcher(reader);
            hits = searcher.search(fullQuery);
        } catch (IOException e) {
            LOG.error("Error when reading indexer stream", e);
        }
        return hits;
    }

    /**
     * This function takes a user's query string and transforms it into a Lucene Query object.
     *
     * @param queryString query
     * @param field       field
     * @param analyzer    Analyzer
     * @return query
     * @throws java.io.IOException exception
     */
    public static Query parseQuery(String queryString, String field, Analyzer analyzer) throws IOException {
        BooleanQuery query = new BooleanQuery();
        TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(queryString));
        Token tok;
        while ((tok = tokenStream.next()) != null) {
            query.add(new PrefixQuery(new Term(field, new String(tok.termBuffer(), 0, tok.termLength()))), BooleanClause.Occur.MUST);
        }
        return query;
    }

    /**
     * This function takes a user's query string  and category and transforms it into
     * category-specific Lucene Query object.  It does this by adding boost values to the query.
     *
     * @param category category
     * @param query    Query
     * @param analyzer analyzer
     * @return Query
     */
    public static Query addCategoryPrefixToQuery(SearchCategory category, Query query, Analyzer analyzer) {

        BooleanQuery prefixQuery = new BooleanQuery();
        List<UrlPattern> urlPattern = category.getUrlPatterns();
        if (analyzer == null) {
            throw new RuntimeException("Analyzer is null");
        }
        if (query == null) {
            throw new RuntimeException("query is null");
        }

        for (UrlPattern pattern : urlPattern) {
            TokenStream tokenStream = analyzer.tokenStream("type", new StringReader(pattern.getType()));

            if (tokenStream == null)
                throw new RuntimeException("tokenStream is null");
            Token token;
            try {
                token = tokenStream.next();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (token != null) {
                TermQuery termQuery = new TermQuery(new Term("type", new String(token.termBuffer(), 0, token.termLength())));

                // add Boost value to get terms to sort properly
                if (pattern.getBoostValue() != 0)
                    termQuery.setBoost(pattern.getBoostValue());
                prefixQuery.add(termQuery, BooleanClause.Occur.SHOULD);
            }
        }
        BooleanQuery fullQuery = new BooleanQuery();
        fullQuery.add(prefixQuery, BooleanClause.Occur.MUST);
        fullQuery.add(query, BooleanClause.Occur.MUST);

        return fullQuery;
    }


}
