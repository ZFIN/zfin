package org.zfin.uniquery.search;

import java.util.Iterator;
import java.util.List;
import org.zfin.uniquery.search.Hit;
import org.apache.lucene.document.Document;

/**
 *  SearchResults
 *
 *  This class object is for storing a set of search results
 *  based on page size (number of results per page).
 */
public class SearchResults
    {
    private Iterator results;
    private int totalHits;
    private int pageSize;
    private int startIndex;

    public SearchResults(Iterator results, int totalHits, int pageSize, int startIndex)
        {            
        this.results = results;
        this.totalHits = totalHits;
        this.pageSize = pageSize;
        this.startIndex = startIndex;
        }

    public Iterator getResults()
        {
        return results;
        }

    public int getTotalHits()
        {
        return totalHits;
        }

    public int getPageSize()
        {
        return pageSize;
        }

    public int getPageCount()
        {
        if (totalHits % pageSize == 0)
            {
            return totalHits / pageSize;
            }
        else
            {
            return (totalHits / pageSize) + 1;
            }
        }

    /**
     * zero based
     */
    public int getCurrentPageIndex()
        {
        return (startIndex / pageSize);
        }


    /*
     * Paea's comments / reminder:
     * This toString() should use StringReader and StringWriter streams for efficiency.
     * Simple String concatenation ("+=") is very very slow in reality.
     */
    public String toString() {
        String htmlOutput = "";
        
        Iterator hitsIterator = results;

        while (hitsIterator.hasNext()) {
            Hit hit = (Hit) hitsIterator.next();
                    System.out.println("************  Got here 3  ************");
            Document doc = hit.getDocument();
            String pageTitle = doc.get(SearchBean.TITLE);
            if (pageTitle.trim().length() < 1)
                {
                pageTitle = "Untitled";
                }
            htmlOutput += "<p>\n";
            htmlOutput += "<a href='" + doc.get(SearchBean.URL) + "'>" + pageTitle + "</a><br>\n";
            htmlOutput += hit.getHighlightedText() + "<br>\n";
            htmlOutput += "<font color='green' size='-2'>" + doc.get(SearchBean.URL) + "</font>\n";
            htmlOutput += "<p>\n";
        }
        
        return htmlOutput;
    }

    }
