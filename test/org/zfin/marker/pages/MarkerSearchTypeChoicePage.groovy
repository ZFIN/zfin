package org.zfin.marker.pages

import geb.Page

class MarkerSearchTypeChoicePage extends Page {
    static at = { title == "ZFIN Marker Search Results" && $('.marker-search-type-choice') }

    static content = {
        geneResultsLink { $('a', text: startsWith('Gene')) }
        transcriptResultsLink { $('a', text: startsWith('Transcript')) }
        searchForm { module(SearchFormModule) }
    }
}
