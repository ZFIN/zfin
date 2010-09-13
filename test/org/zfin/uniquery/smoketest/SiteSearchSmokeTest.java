package org.zfin.uniquery.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.zfin.AbstractSmokeTest;
import org.zfin.uniquery.presentation.SearchBean;

import java.io.IOException;
import java.util.List;

/**
 */
public class SiteSearchSmokeTest extends AbstractSmokeTest {

    /**
     * search for 'cadherin':
     * Should bring up an Alternative search:  calcium-dependent cell adhesion molecule activity (cadherin)
     */
    public void testCadherin() {
        for (WebClient webClient : publicWebClients) {
            try {
                String query = "cadherin";
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch?query=" + query);
                webClient.setJavaScriptEnabled(true);
                webClient.waitForBackgroundJavaScriptStartingBefore(200);

                // find alternative search field
                assertAlternativeTermLinePresent(page);
                assertSingleAlternativeTermName(page, "calcium-dependent cell adhesion molecule activity");

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * search for 'gsc':
     * Should bring up no Alternative search
     */
    public void testGsc() {
        for (WebClient webClient : publicWebClients) {
            try {
                String query = "gsc";
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch?query=" + query);
                webClient.setJavaScriptEnabled(true);
                webClient.waitForBackgroundJavaScriptStartingBefore(200);

                // find alternative search field
                assertAlternativeSearchLineAbsent(page);

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * search for 'nitr':
     * Should bring up no Alternative search. Used to suggest the sequence similarity name 'si:...'.
     */
    public void testNitr() {
        for (WebClient webClient : publicWebClients) {
            try {
                String query = "nitr";
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch?query=" + query);
                webClient.setJavaScriptEnabled(true);
                webClient.waitForBackgroundJavaScriptStartingBefore(200);

                // find alternative search field
                assertAlternativeSearchLineAbsent(page);

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * search for 'midbrain-hindbrain boundary neural tube':
     * Should bring up an Alternative search term:  midbrain hindbrain boundary neural tube
     */
    public void testMidbrainHindbrain() {
        for (WebClient webClient : publicWebClients) {
            try {
                String query = "midbrain-hindbrain boundary neural tube";
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/quicksearch?query=" + query);
                webClient.setJavaScriptEnabled(true);
                webClient.waitForBackgroundJavaScriptStartingBefore(200);

                // find alternative search field
                assertAlternativeTermLinePresent(page);
                assertSingleAlternativeTermName(page, "midbrain hindbrain boundary neural tube");

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    private void assertAlternativeSearchLineAbsent(HtmlPage page) {
        HtmlElement alternativeSearchLines = page.getElementById(SearchBean.ALTERNATIVE_SEARCH_ID);
        assertNull("There is no alternative search line", alternativeSearchLines);
    }

    private void assertSingleAlternativeTermName(HtmlPage page, String anchorText) {
        List<?> relatedTerms = page.getByXPath("//a[@id='" + SearchBean.ALIAS_TERM_ID + "']");
        assertEquals("There is only one alternate search term", 1, relatedTerms.size());
        HtmlAnchor anchor = (HtmlAnchor) relatedTerms.get(0);
        assertEquals("Alternate search term is", anchorText, anchor.getTextContent());
    }

    private void assertAlternativeTermLinePresent(HtmlPage page) {
        HtmlElement alternativeSearchLines = page.getElementById(SearchBean.ALTERNATIVE_SEARCH_ID);
        assertNotNull("There is only one alternative search line", alternativeSearchLines);
    }

}