
package org.zfin.uniquery.search;

import java.util.Iterator;
import java.util.List;


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


    }
