package org.zfin.marker

import geb.spock.GebSpec
import org.zfin.AbstractZfinSmokeSpec
import org.zfin.marker.Pages.MarkerSearchPage
import org.zfin.marker.Pages.MarkerSearchResultsPage
import org.zfin.marker.Pages.MarkerSearchTypeChoicePage

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
           nameField = 'fgf8a'
           searchButton.click()

        then:
           at MarkerSearchTypeChoicePage

        when:
            transcriptResultsLink.click()

        then:
           at MarkerSearchResultsPage

    }
}