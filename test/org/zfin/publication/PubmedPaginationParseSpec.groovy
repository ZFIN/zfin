package org.zfin.publication

import groovy.xml.XmlSlurper
import spock.lang.Specification

/**
 * Regression test for ZFIN-10241.
 *
 * server_apps/data_transfer/PUBMED/fetchPubsFromPubMed.groovy used to extract
 * the page number with `article.Pagination.text()`. PubMed's <Pagination> for
 * article-number papers (Comm Bio, JBMR plus, iScience, Nature Communications,
 * etc.) contains multiple children — typically <StartPage>, <EndPage>, and
 * <MedlinePgn> — all repeating the same article number. Calling .text() on the
 * parent concatenates all descendant text, producing 'eadf5142eadf5142eadf5142'
 * instead of 'eadf5142'.
 *
 * The fix targets the <MedlinePgn> child explicitly, matching what
 * server_apps/DB_maintenance/pub_check_and_addback_volpg.pl already does.
 *
 * The PUBMED Groovy scripts aren't on the compile classpath, so we re-run the
 * relevant XML-slurper expressions here against representative sample inputs
 * rather than invoking processArticle directly.
 */
class PubmedPaginationParseSpec extends Specification {

    def "Pagination.MedlinePgn.text() yields single value for article-number papers"() {
        given: "an article-number style <Pagination> as PubMed serves it (multiple children, all repeating the article id)"
        def article = new XmlSlurper().parseText("""
            <Article>
              <Pagination>
                <StartPage>eadf5142</StartPage>
                <EndPage>eadf5142</EndPage>
                <MedlinePgn>eadf5142</MedlinePgn>
              </Pagination>
            </Article>
        """)

        expect: "the fix expression returns the single article number"
        article.Pagination.MedlinePgn.text() == "eadf5142"

        and: "the broken expression — preserved here as a regression sentinel — is the concatenation we used to write to pub_pages"
        article.Pagination.text() == "eadf5142eadf5142eadf5142"
    }

    def "Pagination.MedlinePgn.text() preserves real page ranges"() {
        given: "a page-range <Pagination> as PubMed serves it for traditional issues"
        def article = new XmlSlurper().parseText("""
            <Article>
              <Pagination>
                <MedlinePgn>18804-18821</MedlinePgn>
              </Pagination>
            </Article>
        """)

        expect: "the fix expression returns the range unchanged"
        article.Pagination.MedlinePgn.text() == "18804-18821"
    }

    def "Pagination.MedlinePgn.text() returns empty for articles with no pages yet"() {
        given: "an Epub-ahead-of-print article whose <Pagination> hasn't been populated yet"
        def article = new XmlSlurper().parseText("""
            <Article>
              <Pagination/>
            </Article>
        """)

        expect: "the fix expression returns an empty string (same as the old behaviour for this case)"
        article.Pagination.MedlinePgn.text() == ""
    }

    def "ELocationID pii fallback picks up pages when <Pagination> is absent"() {
        given: "an article-number paper with no <Pagination> — pages live only in <ELocationID EIdType='pii'> (e.g. PMID 41527836, Hum Mol Genet 35(4):ddaf203)"
        def article = new XmlSlurper().parseText("""
            <Article>
              <ELocationID EIdType="pii">ddaf203</ELocationID>
              <ELocationID EIdType="doi">10.1093/hmg/ddaf203</ELocationID>
            </Article>
        """)

        when: "the loader runs MedlinePgn first and falls back to the pii ELocationID"
        def pages = article.Pagination.MedlinePgn.text()
        if (!pages) {
            pages = article.ELocationID.find { it.@EIdType == 'pii' }?.text() ?: ''
        }

        then: "pages is the pii article number, not empty"
        pages == "ddaf203"
    }

    def "ELocationID pii fallback does not override a real MedlinePgn value"() {
        given: "a traditional paper that has both <Pagination><MedlinePgn> and <ELocationID> — MedlinePgn must win"
        def article = new XmlSlurper().parseText("""
            <Article>
              <Pagination>
                <MedlinePgn>271-277</MedlinePgn>
              </Pagination>
              <ELocationID EIdType="pii">S0012-1606(25)00280-5</ELocationID>
              <ELocationID EIdType="doi">10.1016/j.ydbio.2025.09.022</ELocationID>
            </Article>
        """)

        when: "the loader runs MedlinePgn first and falls back to the pii ELocationID"
        def pages = article.Pagination.MedlinePgn.text()
        if (!pages) {
            pages = article.ELocationID.find { it.@EIdType == 'pii' }?.text() ?: ''
        }

        then: "the page range is preserved; pii is ignored"
        pages == "271-277"
    }

    def "ELocationID pii fallback skips Elsevier-style PII identifiers"() {
        given: "an Epub-ahead-of-print Elsevier article — no <Pagination>, and the only pii is the internal S-number (e.g. PMID with Cell 2026 S0092-8674(26)01124-4)"
        def article = new XmlSlurper().parseText("""
            <Article>
              <ELocationID EIdType="pii">S0092-8674(26)01124-4</ELocationID>
              <ELocationID EIdType="doi">10.1016/j.cell.2026.09.022</ELocationID>
            </Article>
        """)

        when: "the loader applies the same fallback as fetchPubsFromPubMed.groovy"
        def pages = article.Pagination.MedlinePgn.text()
        if (!pages) {
            def pii = article.ELocationID.find { it.@EIdType == 'pii' }?.text() ?: ''
            pages = (pii ==~ /S\d{4}-\d{3}[\dXx]\(.*/) ? '' : pii
        }

        then: "pages stays empty so the refresh jobs can fill it in once PubMed assigns pages"
        pages == ""
    }

    def "ELocationID pii fallback keeps other article ids, including long numeric and dotted ones"() {
        when: "the loader applies the fallback to a pii"
        def article = new XmlSlurper().parseText("""<Article><ELocationID EIdType="pii">${pii}</ELocationID></Article>""")
        def pages = article.Pagination.MedlinePgn.text()
        if (!pages) {
            def p = article.ELocationID.find { it.@EIdType == 'pii' }?.text() ?: ''
            pages = (p ==~ /S\d{4}-\d{3}[\dXx]\(.*/) ? '' : p
        }

        then:
        pages == pii

        where:
        pii << ["dev205615", "e202603687", "492", "1800112", "gr.280355.124", "ENEURO.0408-19.2020"]
    }
}
