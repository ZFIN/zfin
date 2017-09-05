package org.zfin.ontology.presentation

import org.zfin.AbstractZfinSmokeSpec

import static spock.util.matcher.HamcrestSupport.expect
import static org.hamcrest.Matchers.*

class OntologyWebSpec extends AbstractZfinSmokeSpec {

    def "ontology details page by term name"() {
        when:
        def termName = "mitochondrion"
        to TermDetailPage, "term", name: termName, ontologyName: "cellular_component"

        then:
        title == "ZFIN GO: Cellular Component: $termName"
        expect ontologyName.text(), startsWith("GO: Cellular Component")
        expect ontologyLinks.size(), is(2)  // hasSize() doesn't work here
        expect ontologyLinks[0].text(), is("QuickGO")
        expect ontologyLinks[1].text(), is("AmiGO")
        expect termDefinition.text(), startsWith("A semiautonomous")
        expect isTypeOfRow.text(), containsString("cytoplasmic part")
    }

    def "ontology details page by term id"() {
        when:
        to TermDetailPage, "GO:0032502"

        then:
        title == "ZFIN GO: Biological Process: developmental process"
        expect ontologyName.text(), startsWith("GO: Biological Process")
        expect termSynonyms.size(), is(1)
        expect termSynonyms.text(), containsString("development")
    }

    def "post-composed statement page"() {
        when:
        to PostComposedPage, superTermID: "ZFA:0001338", subTermID: "GO:0005737"

        then:
        at PostComposedPage
        title == "ZFIN Post-Composed Term: intestine cytoplasm"
        expect termName.text(), allOf(containsString("intestine"), containsString("cytoplasm"))
    }

    def "post-composed statement popup"() {
        when:
        to PostComposedPopup, superTermID: "ZFA:0001338", subTermID: "GO:0005737"

        then:
        at PostComposedPopup
        title == ""
        expect termName.text(), allOf(containsString("intestine"), containsString("cytoplasm"))
    }

}
