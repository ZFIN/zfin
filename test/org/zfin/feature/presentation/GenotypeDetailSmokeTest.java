package org.zfin.feature.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class GenotypeDetailSmokeTest extends AbstractSmokeTest {


    public GenotypeDetailSmokeTest(WebClient webClient) {
        super();
        this.webClient = webClient;
    }

    private String pageUrl = "/action/genotype/view/ZDB-GENO-050916-1";

    @Test
    public void testGenotypeDetailPage() throws IOException {
        webClient.waitForBackgroundJavaScript(2000);
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + pageUrl);
        webClient.waitForBackgroundJavaScriptStartingBefore(1000);
        assertEquals("Genotype: hand2^s40", page.getTitleText());

        //get a feature link by zdb_id put in the dom id field...
        HtmlAnchor featureLink = (HtmlAnchor) page.getByXPath("//a[@id='ZDB-ALT-050916-2']").get(0);
        assertNotNull(featureLink);
    }

    @Test
    public void testGenotypeDetailPageWhenLoggedInAsRoot() throws IOException {
        webClient.waitForBackgroundJavaScript(2000);
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + pageUrl);
        webClient.waitForBackgroundJavaScriptStartingBefore(1000);
        assertEquals("Genotype: hand2^s40", page.getTitleText());

        //get a feature link by zdb_id put in the dom id field...
        HtmlAnchor featureLink = (HtmlAnchor) page.getByXPath("//a[@id='ZDB-ALT-050916-2']").get(0);
        assertNotNull(featureLink);
    }

    @Test
    public void testAllWildtypePAge() throws IOException {
        String pageUrl = "/action/feature/wildtype-list";
        webClient.waitForBackgroundJavaScript(2000);
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + pageUrl);
        webClient.waitForBackgroundJavaScriptStartingBefore(1000);
        assertEquals("ZFIN: Wild-Type Lines: Summary Listing", page.getTitleText());

        // check that AB genotype is listed.
        List<Object> links = (List<Object>) page.getByXPath("//a[@id='ZDB-GENO-960809-7']");
        assertNotNull(links);
        assertEquals(1, links.size());
    }

}