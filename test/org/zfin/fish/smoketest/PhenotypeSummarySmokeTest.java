package org.zfin.fish.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class PhenotypeSummarySmokeTest extends AbstractSmokeTest {

    public PhenotypeSummarySmokeTest(WebClient webClient) {
        super(webClient);
    }


    /**
     * Check phenotype summary page for various genes..
     */
    @Test
    public void testPhenotypeSummaryPagetp53Morphos() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/marker/ZDB-GENE-060503-803/phenotype-summary");

        // check that publication Ashraf et al is present.
        assertNotNull(page.getElementById("ZDB-PUB-140108-19"));
        assertThat(page.getAnchorByText("AB + MO2-coq8b + MO4-tp53"), is(notNullValue()));
    }

    /**
     * Check that phenotype figure summary page comes up. This case specifically tests heat shcok innocuous TG. (Refer CLNDTY-8)
     */
    @Test
    public void testPhenotypeSummaryPageHsp70HeatShock() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/marker/ZDB-GENE-001103-3/phenotype-summary");

        // check that fish is present.
        assertNotNull(page.getByXPath("//a[@id='ZDB-FISH-150901-27836']").get(0));
    }

    /**
     * Check that phenotype summary page for mir30a exists.
     */
    @Test
    public void testPhenotypeSummaryPageRegionGenes() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/marker/ZDB-MIRNAG-081210-16/phenotype-summary");

        // check that publication O'Brien et al is present.
        assertNotNull(page.getElementById("ZDB-PUB-140513-339"));
        assertThat(page.getAnchorByText("s896Tg; y7Tg + MO1-mir30a"), is(notNullValue()));
    }
}

