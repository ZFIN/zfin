package org.zfin.search.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;

import java.util.List;

/**
 * Default ordering for the Fish category of the faceted search when nothing has been typed in the search box.
 * <p>
 * With an empty query every Fish document has the same text score, so the order is decided by the boost
 * functions in solrconfig.xml. The only one that actually differs between fish is the image boost, which lists
 * reporter lines (the only fish with expression images) ahead of mutants (ZFIN-10392). The ordering here is
 * explicit instead:
 * <ol>
 *   <li>reporter lines after everything else,</li>
 *   <li>the established fish ordering ({@code fish.fish_order}, indexed as {@code complexity}),</li>
 *   <li>name.</li>
 * </ol>
 * To change the ordering later (for example a dedicated sort field populated by the Solr import), only
 * {@link #SORT_CLAUSES} needs to change.
 */
public final class FishSearchSort {

    static final String FISH_CATEGORY = "Fish";

    /** 0 for every fish except reporter lines, 1 for reporter lines. */
    static final String REPORTER_LINE_DEMOTION =
            "if(exists(query({!v='category:\"Reporter Line\"'})),1,0)";

    static final List<SolrQuery.SortClause> SORT_CLAUSES = List.of(
            SolrQuery.SortClause.asc(REPORTER_LINE_DEMOTION),
            SolrQuery.SortClause.asc("complexity"),
            SolrQuery.SortClause.asc("name_sort"));

    private FishSearchSort() {
    }

    /**
     * @param category the selected category
     * @param q        the user's query text
     * @param sort     the explicit sort chosen by the user, if any
     */
    public static boolean appliesTo(String category, String q, String sort) {
        return StringUtils.equals(category, FISH_CATEGORY)
                && StringUtils.isBlank(q)
                && StringUtils.isEmpty(sort);
    }

    /**
     * Adds the Fish ordering to the query if it applies.
     *
     * @return true if the sort was added
     */
    public static boolean applyTo(SolrQuery query, String category, String q, String sort) {
        if (!appliesTo(category, q, sort)) {
            return false;
        }
        SORT_CLAUSES.forEach(query::addSort);
        return true;
    }
}
