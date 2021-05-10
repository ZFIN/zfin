package org.zfin.search.presentation

import geb.Page
import org.zfin.properties.ZfinPropertiesEnum


class SearchResultsPage extends Page {

    static url = "http://${ZfinPropertiesEnum.DOMAIN_NAME}/action/quicksearch/prototype"
    static content = {
        searchBox { $(id: "primary-query-input") }
        submitButton { $("button", type: "submit") }
        searchHelp(required: false) { $(class: "search-help-container") }
        searchResults(required: false) { $(class: "search-result") }
        searchResultNames(required: false, wait: true) { $(class: "search-result-name") }
    }

}
