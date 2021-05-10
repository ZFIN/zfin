package org.zfin.marker

import org.zfin.AbstractZfinSmokeSpec
import org.zfin.marker.pages.MarkerSearchPage
import org.zfin.marker.pages.MarkerSearchResultsPage
import org.zfin.marker.pages.MarkerSearchTypeChoicePage

/**
 * Trying out a Geb/Spock functional (smoke) test.
 */
class MarkerselectWebSpec extends AbstractZfinSmokeSpec {

    def "marker search for fgf8a"() {
        when: "go to the marker search page"
        to MarkerSearchPage

        then: "should find the input box"
        at MarkerSearchPage

        when:
        searchForm.nameField = 'fgf8a'
        searchForm.searchButton.click()

        then:
        at MarkerSearchTypeChoicePage

        when:
        transcriptResultsLink.click()

        then:
        at MarkerSearchResultsPage

    }
}