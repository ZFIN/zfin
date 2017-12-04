package org.zfin.marker.pages

import geb.Page

/**
 * MarkerSearchResultsPage has the form elements from the regular search page, along with the results section.
 *
 * */
class MarkerSearchResultsPage extends Page {
    static at = { title == "ZFIN Marker Search Results" && $('.searchresults') }

    static content = {
        searchForm { module(SearchFormModule) }
    }
}
