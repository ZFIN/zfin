package org.zfin.marker.Pages

/**
 * MarkerSearchResultsPage has the form elements from the regular search page, along with the results section.
 *
* */

class MarkerSearchResultsPage extends MarkerSearchPage {

    static at = { title == "ZFIN Marker Search Results" && $('table.markerselect-results-table') }

}
