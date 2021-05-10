package org.zfin.gwt
import org.zfin.AbstractZfinSmokeSpec
import org.zfin.figure.presentation.AllFigureViewPage
import org.zfin.figure.presentation.FigureViewPage
import spock.lang.Shared
import spock.lang.Unroll

/**
 * Created to test figure view page
 */
class GwtEditSpec extends AbstractZfinSmokeSpec {


    @Unroll
    def "Login page should load, link to all-figure-view, then all-figure-view should load ok"() {
        when: "navigate to the figure page"
        to LoginPage  //this passes figZdbID as an argument to the page, in the url

        then: "page should load"
        at LoginPage

        when: "click on the additional figures link"
        additionalFiguresLink.click()

        then:
        waitFor { at AllFigureViewPage }

    }

    @Unroll
    def '#figZdbID should correct title, gene information'() {
        when:
        to FigureViewPage, figZdbID

        then:
        title == "ZFIN Figure: Ochi et al., 2008, Fig. $figNum"
        $(id: geneID)
        $(id: geneID).find('span')
        $(id: geneID).find('span').attr('title') == geneTitle

        where:
        figZdbID            | figNum | geneID                | geneTitle
        'ZDB-FIG-080508-24' | '1'    | 'ZDB-GENE-980526-561' | 'myogenic differentiation 1'
        'ZDB-FIG-080521-3'  | 'S2'   | 'ZDB-GENE-080509-2'   | 'SWI/SNF related, matrix associated, actin dependent regulator of chromatin, subfamily d, member 3b'
    }

}
