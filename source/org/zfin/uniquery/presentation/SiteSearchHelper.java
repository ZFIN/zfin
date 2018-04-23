package org.zfin.uniquery.presentation;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.zfin.infrastructure.ActiveData;
import org.zfin.uniquery.*;
import org.zfin.uniquery.categories.SiteSearchCategories;
import org.zfin.uniquery.search.CategoryHits;
import org.zfin.uniquery.search.Hit;
import org.zfin.uniquery.search.RelatedTermsService;
import org.zfin.uniquery.search.SearchResults;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Helper class to categorize the results from a site search query
 */
public class SiteSearchHelper {

    private List<CategoryHits> allCategoryHitsList;
    private List<Hit> allResultsList;
    private static ZfinAnalyzer analyzer = new ZfinAnalyzer();
    private SearchResults searchResults;

    private String queryString;
    private int start;
    private int numberOfRecords;
    private String categoryID;

    private static final Logger LOG = Logger.getLogger(SiteSearchHelper.class);

    public SiteSearchHelper(SearchBean searchBean) {
        queryString = getQueryTerm(searchBean.getQuery());
        start = searchBean.getFirstRecord() - 1;
        numberOfRecords = searchBean.getMaxDisplayRecordsInteger();
        categoryID = searchBean.getCategoryID();
    }

    private SiteSearchIndexService siteSearchIndexService;
    private RelatedTermsService relatedTermsService;

    /**
     * For a given user query (and a set of indexes), search for the resulting hits.
     * Then, categorize those hits based on the order and categories specified in the
     * SearchCategory class.
     * <p/>
     * To facilitate the notion of "caching" results on a session-basis, we include
     * a check to only perform a new search/categorization if a new query has been submitted.
     * Therefore, we store the query along with the categorized results until a new query is issued.
     */
    private void categorizeSearchResults() {
        try {
            IndexReader reader = siteSearchIndexService.getLuceneQueryService().getIndexReader();
            Searcher searcher = new IndexSearcher(reader);

            Query query = SiteSearchService.parseQuery(queryString, SiteSearchService.BODY, analyzer);

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
                        CategoryHits catHits = new CategoryHits(category, hits, query, SiteSearchService.MAX_RESULTS_PER_CATEGORY);

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
     */
    public void doFullSearch() {

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults();  // caching should ensure adequate performance

        // from all results, get the relevant results subset based on the pageSize (from x to y out of a total of z)
        List relevantResults = getResultsSubset(allResultsList);

        // now create the resulting HTML results subset
        searchResults = new SearchResults(relevantResults.iterator(), allResultsList.size());
    }

    /**
     * This function returns a subset of results to be displayed given
     * the full results, current view, and page size.
     * <p/>
     * PageSize is the number of results requested per page by the user (default is 25 as of this comment).
     *
     * @param results results
     * @return list of Hit objects
     */
    private List<Hit> getResultsSubset(List<Hit> results) {
        List<Hit> newResults = new ArrayList<Hit>();

        if (start > results.size()) {
            start = 0;
        }

        int lastIndex = start + numberOfRecords;

        if (lastIndex > results.size()) {
            lastIndex = results.size();
        }

        for (int i = start; i < lastIndex; i++) {
            newResults.add(results.get(i));
        }

        return newResults;
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
        categorizeSearchResults();  // caching should ensure adequate performance

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
     * So that a user can browse-by-category, we first perform a full search, getting all results
     * in all categories, and cache the results.
     * <p/>
     * Then, we selectively display the results based on category selected and pageSize.
     *
     * @return SearchResult object
     */
    public void doCategorySearch() {
        if (searchResults != null)
            return;

        // first search for and categorize all results into the category-results structure
        categorizeSearchResults();  // caching should ensure adequate performance

        // category could be "All"
        if (categoryID.equalsIgnoreCase("All")) {
            doFullSearch();
            return;
        }

        // now use the categoryId to filter all the results to only those in that category
        // a better, more efficient implementation would use a binary search instead of a linear one!
        // (for now, since the list only ever has about 12 items, this is tolerable)
        if (!categoryID.trim().equals("")) {
            for (Object anAllCategoryHitsList : allCategoryHitsList) {
                CategoryHits catHit = (CategoryHits) anAllCategoryHitsList;

                // if the category is found, use those results (then exit for loop)
                if (catHit.getCategory().getId().equals(categoryID)) {
                    // from all results, get the relevant results subset (from x to y out of a total of z)
                    List<Hit> relevantResults = getResultsSubset(catHit.getHitsAsHTML());

                    // now create the resulting HTML results subset
                    searchResults = new SearchResults(relevantResults.iterator(), catHit.getHitsAsHTML().size());
                    break;
                }
            }
        }

        // if all else fails, use a full search without category
        if (searchResults == null) {
            doFullSearch();
        }
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
        Map<String, List<String[]>> aliasHits = relatedTermsService.getAllAliasHits(getQueryTerm());
        String returnResults = "";
        if (aliasHits != null && !aliasHits.isEmpty()) {
            returnResults += "<div class='related_terms' id='related-terms'>";
            returnResults += "<span class='alias_list_header' id='" + SiteSearchService.ALTERNATIVE_SEARCH_ID + "'>Alternative search: </span>";
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
                    String newTermFiltered = relatedTermsService.filterIllegals(newTerm).trim();
                    if (separator) {
                        returnResults += " or ";
                    }
                    returnResults += "<a title='" + matchedText + "' href=\"?query=" + newTermFiltered + "\" id='" + SiteSearchService.ALIAS_TERM_ID + "'><em>" +
                            newTermFiltered + "</em></a> <span class='related_terms_match'>(" + matchedText + ")</span>";
                    separator = true;
                }
                returnResults += "</span>";
            }
            returnResults += "</div>";

        }
        return returnResults;
    }

    public String getQueryTerm() {
        return getQueryTerm(queryString);
    }

    public static String getQueryTerm(String queryString) {
        String queryTerm = queryString;
        if (queryTerm == null)
            queryTerm = "";
        queryTerm = queryTerm.replace("*", " ").replace("+", " ");
        queryTerm = queryTerm.trim();

        return queryTerm;
    }

    /**
     * This function returns a direct data page link as the first hit if the query term
     * matches genes/markers/clones/mutants/anatomy item name/symbol.
     *
     * @return string
     */
    public String getBestMatchHTML() {
        String queryTerm = getQueryTerm();
        String matchId = RelatedTermsService.getBestMatchId(queryTerm);
        String returnResults = "";

        if (matchId.length() > 0) {
            String viewPageUrl;
            if (ActiveData.isValidActiveData(matchId, ActiveData.Type.GENO)) {
                viewPageUrl = "/action/genotype/view/" + matchId;
            } else if (matchId.startsWith("ZDB-ANAT") || matchId.startsWith("ZDB-TERM")) {
                viewPageUrl = "/action/anatomy/term-detail?anatomyItem.zdbID=" + matchId;
            } else {
                viewPageUrl = "/" + matchId;
            }
            returnResults += "<br><span class='best_match'>Exact Match: ";
            returnResults += "<a href='" + viewPageUrl + "'><b>" + queryTerm + "</b></a> ";
            returnResults += "</span>";
        }
        return returnResults;
    }

    public SearchResults getSearchResults() {
        return searchResults;
    }

    public void setSiteSearchIndexService(SiteSearchIndexService siteSearchIndexService) {
        this.siteSearchIndexService = siteSearchIndexService;
    }

    public void setRelatedTermsService(RelatedTermsService relatedTermsService) {
        this.relatedTermsService = relatedTermsService;
    }
}
