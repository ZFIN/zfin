package org.zfin.feature.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.zfin.AbstractSecureSmokeTest;
import org.zfin.AbstractSmokeTest;

import java.util.List;

/**
 */
public class GenotypeDetailSmokeTest extends AbstractSecureSmokeTest {

    private String pageUrl = "/ZDB-GENO-050916-1";

    public void testGenotypeDetailPage() {
        for (WebClient browser : curationWebClients) {
            browser.setJavaScriptEnabled(true);
            try {
                browser.waitForBackgroundJavaScript(2000);
                HtmlPage page = browser.getPage("http://" + domain + pageUrl);
                browser.waitForBackgroundJavaScriptStartingBefore(1000);
                assertEquals("Genotype: hand2^s40", page.getTitleText());

                //get a feature link by zdb_id put in the dom id field...
                HtmlAnchor featureLink = (HtmlAnchor) page.getByXPath("//a[@id='ZDB-ALT-050916-2']").get(0);
                assertNotNull(featureLink);

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    public void testGenotypeDetailPageWhenLoggedInAsRoot() {
        for (WebClient browser : curationWebClients) {
            browser.setJavaScriptEnabled(true);
            try {
                login(browser);
                browser.waitForBackgroundJavaScript(2000);
                HtmlPage page = browser.getPage("http://" + domain + pageUrl);
                browser.waitForBackgroundJavaScriptStartingBefore(1000);
                assertEquals("Genotype: hand2^s40", page.getTitleText());

                //get a feature link by zdb_id put in the dom id field...
                HtmlAnchor featureLink = (HtmlAnchor) page.getByXPath("//a[@id='ZDB-ALT-050916-2']").get(0);
                assertNotNull(featureLink);

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


    public void testAllWildtypePAge() {
        for (WebClient browser : curationWebClients) {
            browser.setJavaScriptEnabled(true);
            String pageUrl = "/action/feature/wildtype-list";
            try {
                browser.waitForBackgroundJavaScript(2000);
                HtmlPage page = browser.getPage("http://" + domain + pageUrl);
                browser.waitForBackgroundJavaScriptStartingBefore(1000);
                assertEquals("ZFIN: Wild-Type Lines: Summary Listing", page.getTitleText());

                // check that AB genotype is listed.
                List<Object> links = (List<Object>) page.getByXPath("//a[@id='ZDB-GENO-960809-7']");
                assertNotNull(links);
                assertEquals(1, links.size());
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

}