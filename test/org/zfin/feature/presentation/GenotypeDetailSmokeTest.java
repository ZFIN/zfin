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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
        assertThat("Page title should be correct", page.getTitleText(), equalTo("ZFIN Genotype: hand2^s40"));

        //get a feature link by zdb_id put in the dom id field...
        HtmlAnchor featureLink = (HtmlAnchor) page.getByXPath("//a[@id='ZDB-ALT-050916-2']").get(0);
        assertThat("Link to ZDB-ALT-050916-2 should exist", featureLink, notNullValue());
    }

    @Test
    public void testGenotypeDetailPageWhenLoggedInAsRoot() throws IOException {
        webClient.waitForBackgroundJavaScript(2000);
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + pageUrl);
        webClient.waitForBackgroundJavaScriptStartingBefore(1000);
        assertThat("Page title should be correct", page.getTitleText(), equalTo("ZFIN Genotype: hand2^s40"));

        //get a feature link by zdb_id put in the dom id field...
        HtmlAnchor featureLink = (HtmlAnchor) page.getByXPath("//a[@id='ZDB-ALT-050916-2']").get(0);
        assertThat("Link to ZDB-ALT-050916-2 should exist", featureLink, notNullValue());
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