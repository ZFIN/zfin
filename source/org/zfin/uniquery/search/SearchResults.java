package org.zfin.uniquery.search;

import org.apache.lucene.document.Document;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniquery.presentation.SearchBean;

import java.util.Iterator;

/**
 * SearchResults
 * <p/>
 * This class object is for storing a set of search results
 * based on page size (number of results per page).
 */
public class SearchResults {
    private Iterator results;
    private int totalHits;

    public SearchResults(Iterator results, int totalHits) {
        this.results = results;
        this.totalHits = totalHits;
    }

    public Iterator getResults() {
        return results;
    }

    public int getTotalHits() {
        return totalHits;
    }

    /*
     * Paea's comments / reminder:
     * This toString() should use StringReader and StringWriter streams for efficiency.
     * Simple String concatenation ("+=") is very very slow in reality.
     */
    public String toString() {
        //String htmlOutput = "";
        StringBuffer htmlOutputBuffer = new StringBuffer();

        Iterator hitsIterator = results;

        while (hitsIterator.hasNext()) {
            Hit hit = (Hit) hitsIterator.next();

            Document doc = hit.getDocument();
            String pageTitle = doc.get(SearchBean.TITLE);
            String searchResultURL = doc.get(SearchBean.URL);

            if (pageTitle.trim().length() < 1) {
                pageTitle = "Untitled";
            }

            // there is no mutant name for production, and we want to redirect to cgi-bin/
            //System.out.println(envWebdriverPathFromRoot);

            String envWebdriverLoc = ZfinPropertiesEnum.WEBDRIVER_LOC.value() ;
            // this replacement for webdriver URLs only (java does not encode an instance-specific string in
            // the url.
            // Assumes /<mutant-name>/webdriver?xxx syntax
            if (searchResultURL.indexOf("webdriver") != -1) {
                int webdriver = searchResultURL.indexOf("webdriver");
                String toBeReplaced = searchResultURL.substring(1, webdriver - 1);
                searchResultURL = searchResultURL.replaceFirst(toBeReplaced, envWebdriverLoc);
            }

            htmlOutputBuffer.append("<p>\n");
            htmlOutputBuffer.append("<a href='" + searchResultURL + "'>" + pageTitle + "</a><br>\n");
            htmlOutputBuffer.append(hit.getHighlightedText() + "<br>\n");
            htmlOutputBuffer.append("<font color='green' size='-2'>" + doc.get(SearchBean.URL) + "</font>\n");
            htmlOutputBuffer.append("<p>\n");

        }

        return htmlOutputBuffer.toString();
    }

}
