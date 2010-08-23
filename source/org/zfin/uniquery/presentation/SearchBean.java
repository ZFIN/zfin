package org.zfin.uniquery.presentation;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniquery.SearchCategory;
import org.zfin.uniquery.SiteSearchService;
import org.zfin.uniquery.ZfinAnalyzer;
import org.zfin.uniquery.categories.SiteSearchCategories;
import org.zfin.uniquery.search.CategoryHits;
import org.zfin.uniquery.search.Hit;
import org.zfin.uniquery.search.RelatedTerms;
import org.zfin.uniquery.search.SearchResults;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

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

    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String TYPE = "type";
    public static final int MAX_RESULTS_PER_CATEGORY = 5000;

    private List<CategoryHits> allCategoryHitsList;
    private String queryString;
    private List<Hit> allResultsList;
    private static ZfinAnalyzer analyzer = new ZfinAnalyzer();

    // parameters set by web page query
    private String query;
    private String categoryID;
    private int pageSize = 25;

    // ***********************************

    private static String indexDirectory = ZfinPropertiesEnum.INDEXER_DIRECTORY.value();
    private ReplacementZdbID replacementZdbID;
    private SearchResults searchResult;
    public static final String WEBDRIVER_LOCATION = ZfinPropertiesEnum.WEBDRIVER_LOC.value() ;
    public static final String ALTERNATIVE_SEARCH_ID = "alternative-search";
    public static final String ALIAS_TERM_ID = "alias-term";

    /**
     * For a given user query (and a set of indexes), search for the resulting hits.
     * Then, categorize those hits based on the order and categories specified in the
     * SearchCategory class.
     * <p/>
     * To facilitate the notion of "caching" results on a session-basis, we include
     * a check to only perform a new search/categorization if a new query has been submitted.
     * Therefore, we store the query along with the categorized results until a new query is issued.
     *
     * @param queryString query
     */
    private void categorizeSearchResults(String queryString) {
        try {
            IndexReader reader = IndexReader.open(indexDirectory);
            Searcher searcher = new IndexSearcher(reader);

            Query query = SiteSearchService.parseQuery(queryString, BODY, analyzer);

            if ((allCategoryHitsList != null) && (allResultsList != null) && (this.queryString != null) && (this.queryString.equals(queryString))) {
                // do not repeat search, same query is being repeated and we have it saved already
            } else {
                this.queryString = queryString;
                allResultsList = new ArrayList<Hit>();
                allCategoryHitsList = new ArrayList<CategoryHits>();

                // Search through all categories
                for (int i = 0; i < SiteSearchCategories.getAllSearchCategories().size(); i++) {

                    // determine the category
                    SearchCategory category = SiteSearchCategories.getAllSearchCategories().get(i);

                    if (category != null) {
                        // reformulate query into the category-specific query, and rewrite the original query
                        Query fullQuery = SiteSearchService.addCategoryPrefixToQuery(category, query, analyzer);
                        query = query.rewrite(reader);

                        // search the indexes and get all the hits
                        Hits hits = searcher.search(fullQuery);

                        // save the hits for this category in a category-specific results object
                        CategoryHits catHits = new CategoryHits(category, hits, query, MAX_RESULTS_PER_CATEGORY);

                        // for each category, store the resulting hits in a list
                        allCategoryHitsList.add(catHits);

                        // also for each category, store the resulting HTML results in a list
                        allResultsList.addAll(catHits.getHitsAsHTML());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * In the past, results were not cached.  Every query was re-evaluated and searched, every time.
     * Sometimes, it was on a per-category basis, sometimes for all categories.
     * <p/>
     * Now, however, because navigation based on category browsing has been found to be more useful,
     * we need to always perform a full (all-categories) search so we can facilitate a browse-by-category
     * approach.
     *
     * @param queryString     query
     * @param resultsPageSize int
     * @param startIndex      int
     * @return SearchResult object
     */
    public SearchResults doFullSearch(String queryString, int resultsPageSize, int startIndex) {

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults(queryString);  // caching should ensure adequate performance

        // from all results, get the relevant results subset based on the pageSize (from x to y out of a total of z)
        List relevantResults = getResultsSubset(allResultsList, resultsPageSize, startIndex);

        // now create the resulting HTML results subset
        return new SearchResults(relevantResults.iterator(), allResultsList.size());
    }

    /**
     * So that a user can browse-by-category, we first perform a full search, getting all results
     * in all categories, and cache the results.
     * <p/>
     * Then, we selectively display the results based on category selected and pageSize.
     *
     * @return SearchResult object
     */
    public SearchResults doCategorySearch() {

        String queryString = getQueryTerm();
        String categoryId = getCategoryID();

        SearchResults resultPage = null;

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults(queryString);  // caching should ensure adequate performance

        // category could be "All"
        int startIndex = getFirstRecord() - 1;
        if (categoryId.equalsIgnoreCase("All")) {
            return doFullSearch(queryString, pageSize, startIndex);
        }

        // now use the categoryId to filter all the results to only those in that category
        // a better, more efficient implementation would use a binary search instead of a linear one!
        // (for now, since the list only ever has about 12 items, this is tolerable)
        if (!categoryId.trim().equals("")) {
            for (Object anAllCategoryHitsList : allCategoryHitsList) {
                CategoryHits catHit = (CategoryHits) anAllCategoryHitsList;

                // if the category is found, use those results (then exit for loop)
                if (catHit.getCategory().getId().equals(categoryId)) {
                    // from all results, get the relevant results subset (from x to y out of a total of z)
                    List<Hit> relevantResults = getResultsSubset(catHit.getHitsAsHTML(), pageSize, startIndex);

                    // now create the resulting HTML results subset
                    resultPage = new SearchResults(relevantResults.iterator(), catHit.getHitsAsHTML().size());
                    break;
                }
            }
        }

        // if all else fails, use a full search without category
        if (resultPage == null) {
            resultPage = doFullSearch(queryString, pageSize, startIndex);
        }

        return resultPage;
    }


    /**
     * In the past, this was highly inefficient because it had to perform a search in order
     * to determine the number of results.  In other words, it had to perform a search TWICE:
     * once to get results, and once to get total counts.  This is an artifact of using a
     * REQUEST-based (as opposed to SESSION-based) bean.  The trade-off is performance versus
     * memory usage.
     * <p/>
     * This function returns the number (count) of results found per category by doing a
     * simple lookup in the stored results.  Searching should not occur more than once per query.
     *
     * @param categoryId category
     * @return integer
     */
    public int getSearchResultsCount(String categoryId) {
        int searchResultsCount = 0;

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults(queryString);  // caching should ensure adequate performance

        // category could be "All"
        if (categoryId != null && categoryId.equalsIgnoreCase("ALL")) {
            return allResultsList.size();
        }

        // now use categoryId to find the category and get it's total hit count
        // a better, more efficient implementation would use a binary search instead of a linear one!
        // (for now, since the list only ever has about 12 items, this is tolerable)
        if (categoryId != null && !categoryId.trim().equals("")) {
            for (CategoryHits catHit : allCategoryHitsList) {
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
     * A function that returns the "stop words" or common words that should be ignored
     * given a user query.
     *
     * @param queryString query
     * @return List
     */
    public List<String> getIgnoredWords(String queryString) {
        return findStopWords(queryString, analyzer);
    }


    /**
     * A function that encapsulates the ignored words in HTML formatting for use by the JSP.
     * Ignored words are treated as a separate paragraph, usually one sentence long.
     *
     * @return List of ignored words
     */
    public List<String> getIgnoredWords() {
        return getIgnoredWords(getQueryTerm());
    }

    /**
     * This function supports the ignored words functions.
     *
     * @param queryString query
     * @param analyzer    analyzer
     * @return List
     */
    private List<String> findStopWords(String queryString, ZfinAnalyzer analyzer) {
        List<String> ignoredWords = new ArrayList<String>();
        TokenStream queryTokenStream = analyzer.tokenStream(BODY, new StringReader(queryString));
        TokenStream nonStoppedTokenStream = analyzer.nonStoppedTokenStream(BODY, new StringReader(queryString));

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


    /**
     * This function returns a subset of results to be displayed given
     * the full results, current view, and page size.
     * <p/>
     * PageSize is the number of results requested per page by the user (default is 25 as of this comment).
     *
     * @param results         results
     * @param resultsPageSize page size
     * @param startIndex      starting record
     * @return list of Hit objects
     */
    private List<Hit> getResultsSubset(List<Hit> results, int resultsPageSize, int startIndex) {
        List<Hit> newResults = new ArrayList<Hit>();

        if (startIndex > results.size()) {
            startIndex = 0;
        }

        int lastIndex = startIndex + resultsPageSize;

        if (lastIndex > results.size()) {
            lastIndex = results.size();
        }

        for (int i = startIndex; i < lastIndex; i++) {
            newResults.add(results.get(i));
        }

        return newResults;
    }


    /**
     * This function formats the category list (and counts) as an HTML table.
     * It encapsulates categories in HTML formatting for use by the JSP.
     * <p/>
     * The table layout (number of rows, number of columns, width) can be easily
     * changed using the screenWidth and numberOfColumns variables.
     * <p/>
     * Relies on the "category_table" and "category_item" CSS styles for formatting.
     *
     * @return string
     * @throws java.io.UnsupportedEncodingException
     *          exception
     */
    public String getCategoryListingHTML() throws UnsupportedEncodingException {
        int screenWidth = 100;
        String unitOfMeasure = "%"; // % means percent literally
        int numberOfColumns = 5;
        String cellSelected;
        String categoryHtml;
        List<SearchCategory> categories = SiteSearchCategories.getAllSearchCategories();

        String returnResults = "<div class='category_box'>";
        returnResults += "<TABLE border='0' width='" + screenWidth + unitOfMeasure + "' align='center' cellpadding='2' cellspacing='2' class='category_table'> \n";
        for (int i = 0; i < categories.size(); i++) {
            SearchCategory category = categories.get(i);
            String currentCategoryId = category.getId();
            if (currentCategoryId.equalsIgnoreCase(getCategoryID())) {
                cellSelected = "<img src=/images/right_arrow.gif />&nbsp;";
            } else {
                cellSelected = "";
            }

            if (i % numberOfColumns == 0) {
                returnResults += "<TR> \n";
            }
            int numberOfResults = getSearchResultsCount(currentCategoryId);
            if (currentCategoryId.equalsIgnoreCase("All") || numberOfResults > 1) {
                categoryHtml = "<a href='?pageSize=" + pageSize + "&query=" + URLEncoder.encode(getQueryTerm(), "UTF-8") + "&categoryID=" + currentCategoryId + "'><b>" + category.getDisplayName() + "</b></a>";
            } else if (numberOfResults == 1) {
                String searchResultURL = "";
                for (CategoryHits catHit : allCategoryHitsList) {
                    // if the category is found, use those results (then exit for loop)
                    if (catHit.getCategory().getId().equals(currentCategoryId)) {
                        try {
                            searchResultURL = catHit.getHits().doc(0).get(SearchBean.URL);
                        } catch (IOException e) {
                            LOG.error(e);
                        }
                        break;
                    }
                }
                String envWebdriverLoc = WEBDRIVER_LOCATION;
                searchResultURL = searchResultURL.replaceFirst("almost", envWebdriverLoc);
                categoryHtml = "<a href='" + searchResultURL + "'><b>" + category.getDisplayName() + "</b></a>";

            } else {
                categoryHtml = category.getDisplayName();
            }

            returnResults += "<TD nowrap class='category_item'>";
            returnResults += cellSelected;
            returnResults += categoryHtml;
            if (! (currentCategoryId.equalsIgnoreCase("All") && numberOfResults >= MAX_RESULTS_PER_CATEGORY)) {
                returnResults += " (" + numberOfResults + ")";
            }
            returnResults += "</TD> \n";
        }
        returnResults += "</TABLE> \n";
        returnResults += "</div>";

        return returnResults;

    }

    /**
     * This function formats the Google-like "Alternative search" feature for
     * alias and anatomy term suggestions.
     * It encapsulates this feature in HTML formatting for use by the JSP.
     * <p/>
     * Relies on the "related_terms", "alias_list_header", "related_terms_match",
     * and "alias_list" CSS styles for formatting.
     *
     * @return string
     */
    public String getRelatedTermsHTML() {
        RelatedTerms terms = new RelatedTerms();
        Map<String, List<String[]>> aliasHits = terms.getAllAliasHits(getQueryTerm());
        String returnResults = "";
        if (aliasHits != null && !aliasHits.isEmpty()) {
            returnResults += "<div class='related_terms' id='related-terms'>";
            returnResults += "<span class='alias_list_header' id='" + ALTERNATIVE_SEARCH_ID + "'>Alternative search: </span>";
            Vector<String> keys = new Vector<String>(aliasHits.keySet());
            Collections.sort(keys);
            Iterator aliasKeys = keys.iterator();
            boolean separator = false;
            while (aliasKeys.hasNext()) {
                String oldTerm = (String) aliasKeys.next();
                returnResults += "<span class='alias_list'>";
                List<String[]> matchingTerms = aliasHits.get(oldTerm);
                for (String[] alias_hit : matchingTerms) {
                    String newTerm = alias_hit[0];
                    String matchedText = alias_hit[1];
                    String newTermFiltered = terms.filterIllegals(newTerm).trim();
                    if (separator) {
                        returnResults += " or ";
                    }
                    returnResults += "<a title='" + matchedText + "' href=\"?query=" + newTermFiltered + "\" id='" + ALIAS_TERM_ID + "'><em>" +
                            newTermFiltered + "</em></a> <span class='related_terms_match'>(" + matchedText + ")</span>";
                    separator = true;
                }
                returnResults += "</span>";
            }
            returnResults += "</div>";

        }
        return returnResults;
    }

    /**
     * This function returns a direct data page link as the first hit if the query term
     * matches genes/markers/clones/mutants/anatomy item name/symbol.
     *
     * @return string
     */
    public String getBestMatchHTML() {
        String queryTerm = getQueryTerm();
        String theMatchId = RelatedTerms.getBestMatchId(queryTerm);
        String returnResults = "";
        String webdriverLocation = WEBDRIVER_LOCATION;

        if (theMatchId.length() > 0) {
            String viewPageUrl;
            if (theMatchId.startsWith("ZDB-GENO")) {

               // viewPageUrl = "/cgi-bin/webdriver?MIval=aa-genotypeview.apg&OID=" + theMatchId;
               viewPageUrl = "/action/genotype/detail?genotype.zdbID=" + theMatchId;
            } else if (theMatchId.startsWith("ZDB-ANAT")|| theMatchId.startsWith("ZDB-TERM")) {
                viewPageUrl = "/action/anatomy/term-detail?anatomyItem.zdbID=" + theMatchId;
            } else if (theMatchId.startsWith("ZDB-ATB")) {
                viewPageUrl = "/action/antibody/detail?antibody.zdbID=" + theMatchId;
            } else {
                viewPageUrl = "/" + webdriverLocation + "/webdriver?MIval=aa-markerview.apg&OID=" + theMatchId;
            }
            returnResults += "<br><span class='best_match'>Exact Match: ";
            returnResults += "<a href='" + viewPageUrl + "'><b>" + queryTerm + "</b></a> ";
            returnResults += "</span>";
        }
        return returnResults;
    }


    /**
     * This function formats the suggestion to use specific search forms.
     * It encapsulates this feature in HTML formatting for use by the JSP.
     * <p/>
     * This code adds an unfortunate duplication of effort for category
     * modifications.  By allowing the suggestion to "morph" depending
     * on the category browsed, we have to hard-code information here that
     * will be difficult to maintain in three places:  SearchCategory.java, Indexer.java, and SearchBean.java.
     * <p/>
     * Relies on the "specific_search" CSS styles for formatting.
     *
     * @return string
     * @throws java.io.UnsupportedEncodingException
     *          exception from encoding
     */
    public String getRelatedSearchPageHTML() throws UnsupportedEncodingException {
        String specificSearchURL;
        String categoryDisplayName = SiteSearchCategories.getDisplayName(getCategoryID());
        RelatedTerms terms = new RelatedTerms();
        String queryTerm = getQueryTerm();
        Map<String, List<String>> anatomyHits = terms.getAllAnatomyHits(queryTerm);

        if (categoryDisplayName.toLowerCase().equals("mutants/transgenics")) {
            specificSearchURL = "aa-fishselect.apg&allele_name=" + queryTerm;
        } else if (categoryDisplayName.toLowerCase().equals("genes/markers/clones")) {
            specificSearchURL = "aa-newmrkrselect.apg&input_name=" + queryTerm;
        } else if (categoryDisplayName.toLowerCase().equals("expression/phenotype")) {
            /* expression/phenotyp info are currently all on figure view page, and we
               only have expression search page, no phenotype search page now. */
            specificSearchURL = "aa-xpatselect.apg";
            categoryDisplayName = "Expression";
            if (anatomyHits.size() > 0) {
                specificSearchURL += "&TA_selected_structures=";
                Vector<String> anatkeys = new Vector<String>(anatomyHits.keySet());
                Collections.sort(anatkeys);
                for (String anatkey : anatkeys) {
                    specificSearchURL += URLEncoder.encode(anatkey, "UTF-8") + "%0D%0A"; // %0D%0A = CR-LF
                }
            } else {
                specificSearchURL += "&gene_name=" + queryTerm;
            }
        } else if (categoryDisplayName.toLowerCase().equals("anatomy")) {
            specificSearchURL = "anatomy/search";
            if (anatomyHits.size() > 0) {
                specificSearchURL += "?action=term-search&searchTerm=";
                Vector<String> anatkeys = new Vector<String>(anatomyHits.keySet());
                Collections.sort(anatkeys);
                for (String anatkey : anatkeys) {
                    specificSearchURL += URLEncoder.encode(anatkey, "UTF-8") + " ";
                }
            }
        } else if (categoryDisplayName.toLowerCase().equals("people")) {
            specificSearchURL = "aa-quickfindpers.apg&pname=" + queryTerm;
        } else {
            specificSearchURL = "";
        }

        String returnResults = "";
        if (specificSearchURL.length() > 0) {
            returnResults += "<span class='specific_search'>";
            returnResults += "Advanced search: ";
            if (categoryDisplayName.toLowerCase().equals("anatomy"))
                returnResults += "<a href='/action/" + specificSearchURL + "'>" + categoryDisplayName + "</a> ";
            else
                returnResults += "<a href='/cgi-bin/webdriver?MIval=" + specificSearchURL + "'>" + categoryDisplayName + "</a> ";

            returnResults += "</span>";
        }
        return returnResults;
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

    public String getQueryTerm() {
        String queryTerm = query;
        if (queryTerm == null)
            queryTerm = "";
        queryTerm = queryTerm.replace("*", " ").replace("+", " ");
        queryTerm = queryTerm.trim();

        return queryTerm;
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

    public void setSearchResult(SearchResults searchResult) {
        this.searchResult = searchResult;
    }

    public SearchResults getSearchResult() {
        return searchResult;
    }
}

