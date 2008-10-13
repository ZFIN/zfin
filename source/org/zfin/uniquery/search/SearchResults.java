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
            if(totalHits == 0)
            return 0;
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
        //String htmlOutput = "";
        StringBuffer htmlOutputBuffer = new StringBuffer();

        Iterator hitsIterator = results;

        while (hitsIterator.hasNext()) {
            Hit hit = (Hit) hitsIterator.next();
 
            Document doc = hit.getDocument();
            String pageTitle = doc.get(SearchBean.TITLE);
	    String searchResultURL = doc.get(SearchBean.URL);

            if (pageTitle.trim().length() < 1)
                {
                pageTitle = "Untitled";
                }
	    // if searchResultURL starts with "/cgi-bin_hostname/", 
            // get rid of the hostname
	    // if ( searchResultURL.indexOf("almost") == 0 ) {

	    int pos = searchResultURL.substring(1).indexOf("/");

	    String envWebdriverLoc =  System.getenv("WEBDRIVER_LOC");

     
	    // there is no mutant name for production, and we want to redirect to cgi-bin/
	    //System.out.println(envWebdriverPathFromRoot);
				   		    
	    searchResultURL = searchResultURL.replaceFirst("almost",envWebdriverLoc);
		
		//}
	   
	    htmlOutputBuffer.append("<p>\n");
	    htmlOutputBuffer.append("<a href='" + searchResultURL + "'>" + pageTitle + "</a><br>\n");
	    htmlOutputBuffer.append(hit.getHighlightedText() + "<br>\n");
	    htmlOutputBuffer.append("<font color='green' size='-2'>" + doc.get(SearchBean.URL) + "</font>\n");
	    htmlOutputBuffer.append("<p>\n");

        }
        
        return htmlOutputBuffer.toString();
    }

    }
