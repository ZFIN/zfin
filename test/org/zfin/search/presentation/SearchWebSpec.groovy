package org.zfin.search.presentation

import org.zfin.AbstractZfinSmokeSpec

class SearchWebSpec extends AbstractZfinSmokeSpec {

    def "go to search page with no query"() {
        when:
        to SearchResultsPage

        then:
        searchHelp.displayed
        !searchResults
    }

    def "perform a simple search and check for results"() {
        when:
        to SearchResultsPage, q: "brain"

        then:
        !searchHelp.displayed
        searchResults.size() > 0
        searchResultNames[0].text().startsWith("brain")

    }

}
