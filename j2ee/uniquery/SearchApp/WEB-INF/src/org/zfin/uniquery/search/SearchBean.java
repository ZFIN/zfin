
package org.zfin.uniquery.search;

import org.zfin.uniquery.ZfinAnalyzer;
import org.zfin.uniquery.SearchCategory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.io.StringReader;
import java.io.IOException;



public class SearchBean
    {            
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String TYPE = "type";

    
        
    public SearchResults doCategorySearch(String indexPath, String queryString, String categoryId, int resultsPageSize, int startIndex) throws Exception
        {
        SearchResults results = null;
        List resultsList = new ArrayList();
        IndexReader reader = IndexReader.open(indexPath);
        Searcher searcher = new IndexSearcher(reader);
        ZfinAnalyzer analyzer = new ZfinAnalyzer();
        SearchCategory category = (SearchCategory) SearchCategory.CATEGORY_LOOKUP.get(categoryId);

        //System.err.println("QueryString = " + queryString);
        Query query = parseQuery(queryString, BODY, analyzer);
        //System.err.println("Query = " + query.toString());        
        Query fullQuery = addCategoryPrefixToQuery(category, query, analyzer);
        System.err.println("FullQuery = " + fullQuery.toString());        
        Hits hits = searcher.search(fullQuery);
        
        query = query.rewrite(reader);
        //System.err.println("rewrittenQuery = " + query.toString());
        Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(), new QueryScorer(query));

        if (startIndex > hits.length())
            {
            throw new Exception("Start index is past end of results list");
            }

        int lastIndex = startIndex + resultsPageSize;
        if (lastIndex > (hits.length() - 1))
            {
            lastIndex = hits.length() - 1;
            }

        for (int i=startIndex; i<=lastIndex; i++)
            {
//            String url = hits.doc(i).get(URL);
//            float score = hits.score(i);
//            System.err.println("   => " + score + " -- " + url);
            
            String text = hits.doc(i).get(BODY);
            String highlightedText = "";
            if (text != null)
                {
                TokenStream tokenStream = analyzer.tokenStream(BODY, new StringReader(text));
                highlightedText = highlighter.getBestFragments(tokenStream, text, 3, "...");
                }
            resultsList.add(new Hit(hits.doc(i), hits.score(i), highlightedText));
            }
        results = new SearchResults(resultsList.iterator(), hits.length(), resultsPageSize, startIndex);
        searcher.close();
        return results;
        }




        
    public int getSearchResultsCount(String indexPath, String queryString, String categoryId) throws Exception
        {
        Searcher searcher = new IndexSearcher(indexPath);
        Analyzer analyzer = new ZfinAnalyzer();
        
        SearchCategory category = (SearchCategory) SearchCategory.CATEGORY_LOOKUP.get(categoryId);
        Query query = parseQuery(queryString, BODY, analyzer);
        Query fullQuery = addCategoryPrefixToQuery(category, query, analyzer);
        Hits hits = searcher.search(fullQuery);
        searcher.close();
        return hits.length();
        }

        


    public List getIgnoredWords(String queryString) throws Exception
        {
        ZfinAnalyzer analyzer = new ZfinAnalyzer();
        List ignoredWords = findStopWords(queryString, analyzer);
        return ignoredWords;
        }
        
        
        
        
    private Query parseQuery(String queryString, String field, Analyzer analyzer) throws IOException
        {
        BooleanQuery query = new BooleanQuery();
        TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(queryString));
        Token tok = null;
        while ((tok = tokenStream.next()) != null)
            {
            query.add(new PrefixQuery(new Term(field, tok.termText())), true, false);
            }
        return query;
        }        
    

    
    
    private Query addCategoryPrefixToQuery(SearchCategory category, Query query, Analyzer analyzer) throws IOException
        {
        BooleanQuery prefixQuery = new BooleanQuery();
        String[] types = category.getTypes();
        for (int i=0; i<types.length; i++)
            {
            TokenStream tokenStream = analyzer.tokenStream("type", new StringReader(types[i]));
            TermQuery termQuery = new TermQuery(new Term("type", tokenStream.next().termText()));
            // the huge boost values are to get terms to sort properly
            int boostValue = (int) Math.pow((types.length - i), 8);
            termQuery.setBoost(boostValue);
            prefixQuery.add(termQuery, false, false);
            }
            
        BooleanQuery fullQuery = new BooleanQuery();
        fullQuery.setMaxClauseCount(32000);
        fullQuery.add(prefixQuery, true, false);
        fullQuery.add(query, true, false);
        return fullQuery;
        }
         
         
         
         
     private List findStopWords(String queryString, ZfinAnalyzer analyzer) throws IOException
        {
        ArrayList ignoredWords = new ArrayList();
        TokenStream queryTokenStream = analyzer.tokenStream(BODY, new StringReader(queryString));
        TokenStream nonStoppedTokenStream = analyzer.nonStoppedTokenStream(BODY, new StringReader(queryString));
        
        Token nextQueryToken = null;
        String nextQueryTerm = null;
        Token nextNonStoppedToken = null;
        String nextNonStoppedTerm = null;
        boolean shouldContinue = true;
        while (shouldContinue)
            {
            nextQueryToken = queryTokenStream.next();
            if (nextQueryToken == null)
                {
                nextQueryTerm = null;
                shouldContinue = false;
                }
            else
                {
                nextQueryTerm = nextQueryToken.termText();
                }
                
            while ((nextNonStoppedToken = nonStoppedTokenStream.next()) != null)
                {
                nextNonStoppedTerm = nextNonStoppedToken.termText();
                if (nextNonStoppedTerm.equals(nextQueryTerm))
                    {
                    break;
                    }
                else
                    {
                    ignoredWords.add(nextNonStoppedTerm);
                    }
                }
            }
        return ignoredWords;
        }
        
        
    }

