package org.zfin.uniquery.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;
import org.zfin.uniquery.SiteSearchService;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class SiteSearchSmokeTest extends AbstractSmokeTest {

    public SiteSearchSmokeTest(WebClient webClient) {
        super(webClient);
    }

    /**
     * search for 'cadherin':
     * Should bring up an Alternative search:  calcium-dependent cell adhesion molecule activity (cadherin)
     */
    @Test
    public void testCadherin() throws IOException {
        String query = "cadherin";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch/query?query=" + query);
        webClient.waitForBackgroundJavaScriptStartingBefore(200);

        // find alternative search field
        assertAlternativeTermLinePresent(page);
        assertSingleAlternativeTermName(page, "calcium-dependent cell adhesion molecule activity");
    }

    /**
     * search for 'gsc':
     * Should bring up alternative search term: insulin-responsive compartment
     */
    @Test
    public void testGsc() throws IOException {
        String query = "gsc";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch/query?query=" + query);
        webClient.waitForBackgroundJavaScriptStartingBefore(200);

        // find alternative search field
        assertSingleAlternativeTermName(page, "insulin-responsive compartment");
    }

    /**
     * search for 'nitr':
     * Should bring up no Alternative search. Used to suggest the sequence similarity name 'si:...'.
     */
    @Test
    public void testNitr() throws IOException {
        String query = "nitr";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch/query?query=" + query);
        webClient.waitForBackgroundJavaScriptStartingBefore(200);

        // find alternative search field
        assertAlternativeSearchLineAbsent(page);
    }

    /**
     * search for 'midbrain-hindbrain boundary neural tube':
     * Should bring up an Alternative search term:  midbrain hindbrain boundary neural tube
     */
    @Test
    public void testMidbrainHindbrain() throws IOException {
        String query = "midbrain-hindbrain boundary neural tube";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch/query?query=" + query);
        webClient.waitForBackgroundJavaScriptStartingBefore(200);

        // find alternative search field
        assertAlternativeTermLinePresent(page);
        assertSingleAlternativeTermName(page, "midbrain hindbrain boundary neural tube");
    }

    private void assertAlternativeSearchLineAbsent(HtmlPage page) {
        DomElement alternativeSearchLines = page.getElementById(SiteSearchService.ALTERNATIVE_SEARCH_ID);
        assertNull("There is no alternative search line", alternativeSearchLines);
    }

    private void assertSingleAlternativeTermName(HtmlPage page, String anchorText) {
        List<?> relatedTerms = page.getByXPath("//a[@id='" + SiteSearchService.ALIAS_TERM_ID + "']");
        assertEquals("There is only one alternate search term", 1, relatedTerms.size());
        HtmlAnchor anchor = (HtmlAnchor) relatedTerms.get(0);
        assertEquals("Alternate search term is", anchorText, anchor.getTextContent());
    }

    private void assertAlternativeTermLinePresent(HtmlPage page) {
        DomElement alternativeSearchLines = page.getElementById(SiteSearchService.ALTERNATIVE_SEARCH_ID);
        assertNotNull("There is only one alternative search line", alternativeSearchLines);
    }

}