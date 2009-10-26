package org.zfin.uniquery;

import org.zfin.uniquery.categories.SiteSearchCategories;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class IndexingStatistics {

    private Map<String, Integer> statisticsMap = new HashMap<String, Integer>();

    public IndexingStatistics() {
        init();
    }

    /**
     * Initalize categories.
     */
    private void init() {
        List<SearchCategory> categories = SiteSearchCategories.getAllSearchCategories();
        if (categories == null)
            throw new NullPointerException("Categories not initialized.");
    }

    public void addUrl(String url) {
        String docType = SiteSearchCategories.getDocType(url);
        int number = 0;
        if (statisticsMap.containsKey(docType)) {
            number = statisticsMap.get(docType);
        }
        statisticsMap.put(docType, (number + 1));
    }

    public Map getStatisticsMap() {
        return statisticsMap;
    }
}
