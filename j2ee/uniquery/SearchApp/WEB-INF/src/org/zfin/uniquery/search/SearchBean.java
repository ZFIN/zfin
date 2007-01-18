package org.zfin.uniquery.search;

import org.zfin.uniquery.ZfinAnalyzer;
import org.zfin.uniquery.SearchCategory;
import org.apache.commons.lang.StringUtils;
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
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Collections;
import java.io.StringReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.net.URLEncoder;

/**
 *  The SearchBean is a Bean that is used by the JSP Quick Search tool
 *  to perform a large variety of tasks.
 *
 *  Because a user will most likely perform a query, then browse categories, we should
 *  cache the results and so this bean should be used in a "Session" mode context so
 *  that the results are stored in memory for each user.  As a result, this will
 *  take up a significant amount of system memory.
 */
public class SearchBean
{

    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String TYPE = "type";

    private ArrayList allCategoryHitsList;
    private String categoryId;
    private String queryString;
    private ArrayList allResultsList;

    /** the constructor
     * the jsp:useBean definition seems not pass without
     * a standard constructor
     */
    public SearchBean() {
	super();
    }


    /**
     *  categorizeSearchResults
     *
     *  For a given user query (and a set of indexes), search for the resulting hits.
     *  Then, categorize those hits based on the order and categories specified in the
     *  SearchCategory class.
     *
     *  To facilitate the notion of "caching" results on a session-basis, we include
     *  a check to only perform a new search/categorization if a new query has been submitted.
     *  Therefore, we store the query along with the categorized results until a new query is issued.
     */
    private void categorizeSearchResults(String indexPath, String queryString) throws Exception
    {
        IndexReader reader = IndexReader.open(indexPath);
        Searcher searcher = new IndexSearcher(reader);
        ZfinAnalyzer analyzer = new ZfinAnalyzer();

        //System.err.println("Query = " + query.toString());
        Query query = parseQuery(queryString, BODY, analyzer);
        //System.err.println("rewrittenQuery = " + query.toString());
        Query rewrittenQuery = query.rewrite(reader);


        if ((allCategoryHitsList != null) && (allResultsList != null) && (this.queryString != null) && (this.queryString.equals(queryString))) {
               // do not repeat search, same query is being repeated and we have it saved already
        } else {
            this.queryString = queryString;
            allResultsList = new ArrayList();
            allCategoryHitsList = new ArrayList();

            /*
             * Search all categories in the order of the SearchCategory.CATEGORIES list
             * (which is decided by biologists).
             */
            for (int i=0; i<SearchCategory.CATEGORIES.size(); i++) {
                Hits hits = null;
                Query fullQuery = query;

                // determine the category
                SearchCategory category = (SearchCategory) SearchCategory.CATEGORIES.get(i);

                if (category != null) {
                    // reformulate query into the category-specific query, and rewrite the original query
                    fullQuery = addCategoryPrefixToQuery(category, query, analyzer);
                    query = query.rewrite(reader);
                    //System.err.println("categorizeSearchResults FullQuery = " + fullQuery.toString());

                    // search the indexes and get all the hits
                    hits = searcher.search(fullQuery);

                    // save the hits for this category in a category-specific results object
                    CategoryHits catHits = new CategoryHits(category, hits, query);

                    // for each category, store the resulting hits in a list
                    allCategoryHitsList.add(catHits);

                    // also for each category, store the resulting HTML results in a list
                    allResultsList.addAll(catHits.getHitsAsHTML());
                }
            }
        }
    }


    /**
     *  doFullSearch
     *
     *  In the past, results were not cached.  Every query was re-evaluated and searched, everytime.
     *  Sometimes, it was on a per-category basis, sometimes for all categories.
     *
     *  Now, however, because navigation based on category browsing has been found to be more useful,
     *  we need to always perform a full (all-categories) search so we can faciliate a browse-by-category
     *  approach.
     */
    public SearchResults doFullSearch(String indexPath, String queryString, int resultsPageSize, int startIndex) throws Exception {
        SearchResults resultPage = null;

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults(indexPath, queryString);  // caching should ensure adequate performance

        // from all results, get the relevant results subset based on the pageSize (from x to y out of a total of z)
        ArrayList relevantResults = getResultsSubset(allResultsList, resultsPageSize, startIndex);

        // now create the resulting HTML results subset
        resultPage = new SearchResults(relevantResults.iterator(), allResultsList.size(), resultsPageSize, startIndex);

        return resultPage;
    }

    /**
     *  doCategorySearch
     *
     *  So that a user can browse-by-category, we first peform a full search, getting all results
     *  in all categories, and cache the results.
     *
     *  Then, we selectively display the results based on category selected and pageSize.
     *
     */
    public SearchResults doCategorySearch(String indexPath, String queryString, String categoryId, int resultsPageSize, int startIndex) throws Exception {
        SearchResults resultPage = null;

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults(indexPath, queryString);  // caching should ensure adequate performance

        // category could be "All"
        if (categoryId.equalsIgnoreCase("All")) {
            return doFullSearch(indexPath, queryString, resultsPageSize, startIndex);
        }

        // now use the categoryId to filter all the results to only those in that category
        // a better, more efficient implementation would use a binary search instead of a linear one!
        // (for now, since the list only ever has about 12 items, this is tolerable)
        if (categoryId != null && !categoryId.trim().equals("")) {
            for (int i=0; i < allCategoryHitsList.size(); i++ ) {
                CategoryHits catHit = (CategoryHits) allCategoryHitsList.get(i);

                // if the category is found, use those results (then exit for loop)
                if (catHit.getCategory().getId().equals(categoryId)) {
                    // from all results, get the relevant results subset (from x to y out of a total of z)
                    ArrayList relevantResults = getResultsSubset(catHit.getHitsAsHTML(), resultsPageSize, startIndex);

                    // now create the resulting HTML results subset
                    resultPage = new SearchResults(relevantResults.iterator(), catHit.getHitsAsHTML().size(), resultsPageSize, startIndex);
                    break;
                }
            }
        }

        // if all else fails, use a full search without category
        if (resultPage == null) {
            resultPage = doFullSearch(indexPath, queryString, resultsPageSize, startIndex);
        }

        return resultPage;
    }


    /**
     *  getSearchResultsCount
     *
     *  In the past, this was highly inefficient because it had to perform a search in order
     *  to determine the number of results.  In otherwords, it had to perform a search TWICE:
     *  once to get results, and once to get total counts.  This is an artifact of using a
     *  REQUEST-based (as opposed to SESSION-based) bean.  The tradeoff is performance versus
     *  memory usage.
     *
     *  This function returns the number (count) of results found per category by doing a
     *  simple lookup in the stored results.  Searching should not occur more than once per query.
     *
     */
    public int getSearchResultsCount(String indexPath, String queryString, String categoryId) throws Exception {
        int searchResultsCount = 0;

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults(indexPath, queryString);  // caching should ensure adequate performance

        // category could be "All"
        if (categoryId.equalsIgnoreCase("All")) {
            return allResultsList.size();
        }

        // now use categoryId to find the category and get it's total hit count
        // a better, more efficient implementation would use a binary search instead of a linear one!
        // (for now, since the list only ever has about 12 items, this is tolerable)
        if (categoryId != null && !categoryId.trim().equals("")) {
            for (int i=0; i < allCategoryHitsList.size(); i++ ) {
                CategoryHits catHit = (CategoryHits) allCategoryHitsList.get(i);
                // if the category is found, use those results (then exit for loop)
                if (catHit.getCategory().getId().equals(categoryId)) {
                    searchResultsCount = catHit.getHits().length();
                    break;
                }
            }
        }

        return searchResultsCount;
    }

    /**
     *  getIgnoredWords
     *
     *  A function that returns the "stop words" or common words that should be ignored
     *  given a user query.
     */
    public List getIgnoredWords(String queryString) throws Exception {
        ZfinAnalyzer analyzer = new ZfinAnalyzer();
        List ignoredWords = findStopWords(queryString, analyzer);
        return ignoredWords;
    }


    /**
     *  getIgnoredWordsHTML
     *
     *  A function that encapsulates the ignored words in HTML formatting for use by the JSP.
     *  Ignored words are treated as a separate paragraph, usually one sentence long.
     *
     *  Relies on the "ignored_words" CSS class for formatting.
     */
    public String getIgnoredWordsHTML(String queryString) throws Exception {
        List ignoredWords = getIgnoredWords(queryString);
        String returnResults = "";

        if (ignoredWords.size() > 0) {
            returnResults += "<div class='ignored_words'>";
            returnResults += "The following words are very common and were not included in your search: &nbsp;&nbsp;";

            for (int i=0; i<ignoredWords.size(); i++) {
                String word = (String) ignoredWords.get(i);
                returnResults += "<em>" + word+ "</em>&nbsp;&nbsp;";
            }

            returnResults += "</div>";
        }

        return returnResults;
    }


    /**
     *  parseQuery
     *
     *  This function takes a user's query string and transforms it into a Lucene Query object.
     */
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


    /**
     *  addCategoryPrefixToQuery
     *
     *  This function takes a user's query string  and category and transforms it into
     *  category-specific Lucene Query object.  It does this by adding boost values to the query.
     */
    private Query addCategoryPrefixToQuery(SearchCategory category, Query query, Analyzer analyzer) throws IOException
        {

        BooleanQuery prefixQuery = new BooleanQuery();

        String[] types = category.getTypes();

	if (types == null){
	    throw new RuntimeException("No types found");
	}
	if(analyzer == null){

	    throw new RuntimeException("Analyzer is null");
	}
	if(query == null){

	    throw new RuntimeException("query is null");
	}

        for (int i=0; i<types.length; i++) {

	    TokenStream tokenStream = analyzer.tokenStream("type", new StringReader(types[i]));

	    if(tokenStream == null)
		throw new RuntimeException("tokenStream is null [" + i +"]");
	    Token token = tokenStream.next();
	    if(token == null){
		break;
	    }
	    TermQuery termQuery = new TermQuery(new Term("type", token.termText()));

	    if(termQuery == null)
		throw new RuntimeException("termQuery is null [" + i +"]");
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



    /**
     *  findStopWords
     *
     *  This function supports the ignored words functions.
     */
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


    /**
     *  getResultsSubset
     *
     *  This function returns a subset of results to be displayed given
     *  the full results, current view, and page size.
     *
     *  PageSize is the number of results requested per page by the user (default is 25 as of this comment).
     */
    private ArrayList getResultsSubset(ArrayList results, int resultsPageSize, int startIndex) {
        ArrayList newResults = new ArrayList();

        if (startIndex > results.size()) {
            startIndex = 0;
        }

        int lastIndex = startIndex + resultsPageSize;

        if (lastIndex > results.size()) {
            lastIndex = results.size();
        }

        for (int i=startIndex; i<lastIndex; i++) {
            newResults.add(results.get(i));
        }

        return newResults;
    }


    /**
     *  getAllResultsList
     *
     *  Returns the entire list of search results.
     *  Results are formatted in HTML (though this function
     *  breaks naming convention, it should be called getAllResultsListHTML.)
     */
    public ArrayList getAllResultsList() {
        return allResultsList;
    }


    /**
     *  getCategoryListingHTML
     *
     *  This function formats the category list (and counts) as an HTML table.
     *  It encapsulates categories in HTML formatting for use by the JSP.
     *
     *  The table layout (number of rows, number of columns, width) can be easily
     *  changed using the screenWidth and numberOfColumns variables.
     *
     *  Relies on the "category_table" and "category_item" CSS styles for formatting.
     */
    public String getCategoryListingHTML(String indexPath, String queryTerm, String categoryTerm, int pageSize) throws Exception {
        String returnResults = "";
        int screenWidth = 100;
        String unitOfMeasure = "%"; // % means percent literally
        int numberOfColumns = 5;
	int numberOfResults = 0;
        int columnWidth = screenWidth / numberOfColumns;
        String cellSelected = "";
	String categoryHtml = "";
	String searchResultURL = "";
        ArrayList categories = new ArrayList();
        categories.addAll(SearchCategory.CATEGORIES);

        returnResults += "<div class='category_box'>";
        returnResults += "<TABLE border='0' width='" + screenWidth + unitOfMeasure + "' align='center' cellpadding='2' cellspacing='2' class='category_table'> \n";
        for (int i=0; i<categories.size(); i++) {
            SearchCategory category = (SearchCategory) categories.get(i);
	    String currentCategoryId = category.getId();
            if (currentCategoryId.equalsIgnoreCase(categoryTerm.trim())) {
                cellSelected = "<img src=/images/right_arrow.gif />&nbsp;";
            } else {
                cellSelected = "";
            }

            if (i%numberOfColumns == 0) {
                returnResults += "<TR> \n";
            }
            numberOfResults = getSearchResultsCount(indexPath, queryTerm, currentCategoryId);
	    if (currentCategoryId.equalsIgnoreCase("All") || numberOfResults > 1) {
		categoryHtml = "<a href='category_search.jsp?pageSize=" + pageSize + "&query=" + URLEncoder.encode(queryTerm, "UTF-8") + "&category=" + currentCategoryId + "'><b>" + category.getDescription() + "</b></a>";
	    }
	    else if (numberOfResults == 1) {

		for (int j=0; j < allCategoryHitsList.size(); j++ ) {
		    CategoryHits catHit = (CategoryHits) allCategoryHitsList.get(j);
		    // if the category is found, use those results (then exit for loop)
		    if (catHit.getCategory().getId().equals(currentCategoryId)) {
			searchResultURL = catHit.getHits().doc(0).get(SearchBean.URL);
			break;
		    }
		}
                // if searchResultURL starts with "/cgi-bin_hostname/",
                // get rid of the hostname
		if ( searchResultURL.indexOf("cgi-bin_") > 0 ) {
		    int pos = searchResultURL.substring(1).indexOf("/");
		    searchResultURL = "/cgi-bin" +  searchResultURL.substring(pos+1);
		}

		categoryHtml = "<a href='" + searchResultURL + "'><b>" + category.getDescription() + "</b></a>";

	    }
	    else{
		categoryHtml = category.getDescription() ;
	    }

	    returnResults += "<TD nowrap class='category_item'>";
	    returnResults += cellSelected;
	    returnResults += categoryHtml;
	    returnResults += " (" + numberOfResults  + ")";
	    returnResults += "</TD> \n";
        }
        returnResults += "</TABLE> \n";
        returnResults += "</div>";

        return returnResults;

    }

    /**
     *  getPageNavigationHTML
     *
     *  This function formats the page navigation feature (<PREV 1 2 3 4 NEXT>)
     *  at the bottom of the search page results.
     *  It encapsulates page navigation in HTML formatting for use by the JSP.
     *
     *  This code is unweildly and difficult to modify, it would be nice to
     *  modularize it neatly.
     */
    public String getPageNavigationHTML(SearchResults results, String indexPath, String queryTerm, String categoryTerm, int pageSize) throws Exception {
        String returnResults = "";

        int totalPageCount = results.getPageCount();
        int currentPage = results.getCurrentPageIndex();
        int startOfPageRange = Math.max(0, currentPage - 3);
        int endOfPageRange = Math.min(currentPage + 4, totalPageCount);

        returnResults += "<p align='center'>";

        if (totalPageCount > 1) {
            returnResults +=  "<table width='70%' border='0'> \n";
            returnResults +=  "<tr> \n";
            returnResults +=  "<td width='45%' align='right' valign='top'>&nbsp;";
            if (currentPage > 0) {
                returnResults +=  "<a href='category_search.jsp?query=" + URLEncoder.encode(queryTerm, "UTF-8") + "&category=" + categoryTerm + "&pageSize=" + pageSize + "&startIndex=" + ((currentPage-1) * pageSize) + "'>Previous</a><br>";
                returnResults +=  "<a href='category_search.jsp?query=" + URLEncoder.encode(queryTerm, "UTF-8") + "&category=" + categoryTerm + "&pageSize=" + pageSize + "&startIndex=0'>First Page</a>";
            }
            returnResults +=  "</td>\n";

            for (int i=startOfPageRange; i<endOfPageRange; i++) {
                 if (i == currentPage) {
                    returnResults +=  "<td valign='top'>";
                    returnResults +=  "<b>" + (i + 1) + "&nbsp;</b>";
                    returnResults +=  "</td> \n";
                 } else {
                    returnResults +=  "<td valign='top'>";
                    returnResults +=  "<a href='category_search.jsp?query=" + URLEncoder.encode(queryTerm, "UTF-8") + "&category=" + categoryTerm + "&pageSize=" + pageSize + "&startIndex=" + (i * pageSize) + "'>" + (i + 1) + "&nbsp;</a>";
                    returnResults +=  "</td> \n";
                 }
            }

            returnResults +=  "<td width='45%' align='left' valign='top'>&nbsp;";
            if (currentPage < (totalPageCount - 1)) {
                 returnResults += "<a href='category_search.jsp?query=" + URLEncoder.encode(queryTerm, "UTF-8") + "&category=" + categoryTerm + "&pageSize=" + pageSize + "&startIndex=" + ((currentPage+1) * pageSize) + "'>Next</a><br>";
                 returnResults += "<a href='category_search.jsp?query=" + URLEncoder.encode(queryTerm, "UTF-8") + "&category=" + categoryTerm + "&pageSize=" + pageSize + "&startIndex=" + ((totalPageCount-1) * pageSize) + "'>Last page</a>";
             }
            returnResults +=  "</td> \n";
            returnResults +=  "</tr> \n";
            returnResults +=  "</table> \n";
            }
        returnResults += "</p> \n";

        return returnResults;
    }

    /**
     *  getNewZdbId

     */
    public String getNewZdbId(String dbName, String queryTerm) throws Exception {
        RelatedTerms terms = new RelatedTerms(dbName);
        String replacedId = terms.getReplacedZdbId(queryTerm);
        return  replacedId;
    }

    /**
     *  getRelatedTermsHTML
     *
     *  This function formats the Google-like "Alternative search" feature for
     *  alias and anotomy term suggestions.
     *  It encapsulates this feature in HTML formatting for use by the JSP.
     *
     *  Relies on the "related_terms", "alias_list_header", "related_terms_match",
     *  and "alias_list" CSS styles for formatting.
     */
    public String getRelatedTermsHTML(String dbName, String queryTerm) throws Exception {
        RelatedTerms terms = new RelatedTerms(dbName);
        Hashtable aliasHits = terms.getAllAliasHits(queryTerm);
        String returnResults = "";

        if (aliasHits.size() > 0) {
           returnResults += "<div class='related_terms'>";
           returnResults += "<span class='alias_list_header'>Alternative search: </span>";
           Vector keys = new Vector(aliasHits.keySet());
           Collections.sort(keys);
           Iterator aliaskeys = keys.iterator();
           boolean separator = false;
           while (aliaskeys.hasNext()) {
              String oldTerm = (String) aliaskeys.next();
              returnResults += "<span class='alias_list'>";
              ArrayList matchingTerms = (ArrayList) aliasHits.get(oldTerm);
              for (int i = 0; i < matchingTerms.size(); i++) {
                 String[] alias_hit = (String[]) matchingTerms.get(i);
                 String newTerm = alias_hit[0];
                 String matchedText = alias_hit[1];
                 String newTermFiltered = terms.filterIllegals(newTerm).trim();
                 if (separator) {
                    returnResults += " or ";
                 }
		 returnResults += "<a title='" + matchedText + "' href=\"category_search.jsp?query=" + newTermFiltered + "\"><em>" + newTermFiltered + "</em></a> <span class='related_terms_match'>(" + matchedText + ")</span>";


                 separator = true;
              }
              returnResults += "</span>";
              //returnResults += "</div>";
           }
           returnResults += "</div>";

	}
	return returnResults;
    }

    /**
      * getBestMatchHTML
      *
      * This function returns a direct data page link as the first hit if the query term
      * matches genes/markers/clones/mutants/anatomy item name/symbol.
      *
      */
    public String getBestMatchHTML(String dbName, String queryTerm) throws Exception {
	RelatedTerms term = new RelatedTerms(dbName);
	String theMatchId = term.getBestMatchId(queryTerm);
	String viewPageUrl = "";
	String returnResults = "";

	if (theMatchId.length() > 0) {
	    if (theMatchId.startsWith("ZDB-GENO")) {
		viewPageUrl = "/cgi-bin/webdriver?MIval=aa-genotypeview.apg&OID=" + theMatchId;
	    }
	    else if (theMatchId.startsWith("ZDB-ANAT")) {
		viewPageUrl = "/cgi-bin/webdriver?MIval=aa-anatomy_item.apg&OID=" + theMatchId;
	    }
	    else {
		viewPageUrl = "/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=" + theMatchId;
	    }
	    returnResults += "<br><span class='best_match'>Exact Match: ";
            returnResults += "<a href='" + viewPageUrl + "'><b>" + queryTerm + "</b></a> ";
	    returnResults += "</span>";
	}
	return returnResults;
    }


   /**
     *  getRelatedSearchPageHTML
     *
     *  This function formats the suggestion to use specific search forms.
     *  It encapsulates this feature in HTML formatting for use by the JSP.
     *
     *  This code adds an unfortunate duplication of effort for category
     *  modifications.  By allowing the suggestion to "morph" depending
     *  on the category browsed, we have to hard-code information here that
     *  will be difficult to maintain in three places:  SearchCategory.java, Indexer.java, and SearchBean.java.
     *
     *  Relies on the "specific_search" CSS styles for formatting.
     */
    public String getRelatedSearchPageHTML(String dbName, String queryTerm, String categoryTerm) throws Exception {
        String specificSearchURL = "";
        String categoryDescription = SearchCategory.getDescriptionById(categoryTerm);
        String returnResults = "";
        RelatedTerms terms = new RelatedTerms(dbName);
        Hashtable anatomyHits = terms.getAllAnatomyHits(queryTerm);

        if (categoryDescription.toLowerCase().equals("mutants/transgenics")) {
           specificSearchURL = "aa-fishselect.apg&allele_name="+queryTerm;
        } else if (categoryDescription.toLowerCase().equals("genes/markers/clones")) {
           specificSearchURL = "aa-newmrkrselect.apg&input_name="+queryTerm;
        } else if (categoryDescription.toLowerCase().equals("expression/phenotype")) {
	   /* expression/phenotyp info are currently all on figureview page, and we
             only have expression search page, no phenotype search page now. */
           specificSearchURL = "aa-xpatselect.apg";
	   categoryDescription = "Expression";
           if (anatomyHits.size() > 0) {
             specificSearchURL += "&TA_selected_structures=";
             Vector anatkeys = new Vector(anatomyHits.keySet());
             Collections.sort(anatkeys);
             Iterator anatkeysIter = anatkeys.iterator();
             while (anatkeysIter.hasNext()) {
                String anatTerm = (String) anatkeysIter.next();
                specificSearchURL += URLEncoder.encode(anatTerm) + "%0D%0A"; // %0D%0A = CR-LF
             }
           }else {
	       specificSearchURL += "&gene_name=" + queryTerm;
	   }
        } else if (categoryDescription.toLowerCase().equals("anatomy")) {
           specificSearchURL = "aa-anatdict.apg&mode=search";
           if (anatomyHits.size() > 0) {
             specificSearchURL += "&searchterm=";
             Vector anatkeys = new Vector(anatomyHits.keySet());
             Collections.sort(anatkeys);
             Iterator anatkeysIter = anatkeys.iterator();
             while (anatkeysIter.hasNext()) {
                String anatTerm = (String) anatkeysIter.next();
                specificSearchURL += URLEncoder.encode(anatTerm) + " ";
             }
           }
        } else if (categoryDescription.toLowerCase().equals("people")) {
           specificSearchURL = "aa-quickfindpers.apg&pname="+queryTerm;
        } else {
           specificSearchURL = "";
        }

        if (specificSearchURL.length() > 0) {
            returnResults += "<span class='specific_search'>";
            returnResults += "Advanced search: "; //"Please try the ";
            returnResults += "<a href='/cgi-bin/webdriver?MIval=" + specificSearchURL + "'>" + categoryDescription + "</a> ";

            returnResults += "</span>";
        }
	//else {
        //    returnResults += "Please try the above custom search pages for more specific results.";
        //}

        return returnResults;
    }


    /**
     *  getAdvancedSearchHTML
     *
     *  This function formats advanced search form feature.
     *  It encapsulates this feature in HTML formatting for use by the JSP.
     *
     *  Relies on the "search", "titlebar", and "submitbar" CSS styles for formatting.


     public String getAdvancedSearchHTML(String queryTerm, String categoryTerm, int pageSize) {
        String returnResults = "";

        returnResults += "<TABLE class='search' width='100%' border='0' cellpadding='1' cellspacing='0'>";
        returnResults += "<TR>";
        returnResults += "<TD align='left' class='titlebar'>";

        if (queryTerm.length() > 0) {
            returnResults += "<a name='modify'>";
            returnResults += "<font size='+1'><b>Search ZFIN</b></font>";
            returnResults += "</a>";
        }

        returnResults += "&nbsp;";
        returnResults += "</TD>";
        returnResults += "<TD align='right' class='titlebar'>";
        returnResults += "<a href='syntax_help.jsp'>Search Tips</a>";
        returnResults += "</TD>";
        returnResults += "</TR>";
        returnResults += "<TR>";
        returnResults += "<TD colspan='2'>";
        returnResults += "<form name='uniquery_search' action='/SearchApp/category_search.jsp' method='get'>";
        returnResults += "<p>";
        returnResults += "Search <select name='category'>";

        for (int i=0; i<SearchCategory.CATEGORIES.size(); i++) {
            SearchCategory category = (SearchCategory) SearchCategory.CATEGORIES.get(i);
            String selected = "";
            if (category.getId().equals(categoryTerm)) {
                selected = " SELECTED";
            }
            returnResults += "<option value=" + category.getId() + selected + ">" + category.getDescription() + "</option>";
        }

        returnResults += "</select>";
        returnResults += "for <input type='text' name='query' value='" + StringUtils.replace(queryTerm, "\"", "&quot;") + "' size='34'/> ";
        returnResults += "with <input type='text' name='pageSize' size='3' value='" + pageSize + "'> results per page.";
        returnResults += "</p>";
        returnResults += "<TR>";
        returnResults += "<TD class='submitbar' colspan=2 align='right'>";
        returnResults += "<input type=submit name=search value='SEARCH'/>";
        returnResults += "<input type=button onClick='call_reset();' value='RESET'/>";
        returnResults += "</TD>";
        returnResults += "</TR>";
        returnResults += "</form>";
        returnResults += "</TD>";
        returnResults += "</TR>";
        returnResults += "</TABLE>";

        return returnResults;
     }
    */
    }
