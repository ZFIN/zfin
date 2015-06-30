package org.zfin.fish.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class FishSmokeTest extends AbstractSmokeTest {


    public FishSmokeTest(WebClient webClient) {
        super(webClient);
    }
//TODO: replace with valid Fish id's and remove the @Ignore annotation
    /**
     * Just pull up the fish search form and check for no errors.
     */
    @Test
    public void testFishSearchPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/search");
        assertTrue(page.getTitleText().contains("ZFIN Fish Search"));
    }


    /**
     * Display Fish detail page for
     * Genotype + Morpholinos:  wild type (unspecified)+MO1-shha
     */
    @Test
    @Ignore
    public void testFishDetailPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/fish-detail/ZDB-GENO-030619-2,ZDB-GENOX-090731-5,ZDB-GENOX-130614-8,ZDB-GENOX-141110-7");
        assertTrue(page.getTitleText().contains("ZFIN Fish: WT"));
    }

    /**
     * Display Citation list for
     * Genotype + Morpholinos:  wild type (unspecified)+MO1-shha
     */
    @Test
    @Ignore
    public void testFishCitationList() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/fish-publication-list?fishID=ZDB-GENO-030619-2,ZDB-GENOX-090731-5,ZDB-GENOX-130614-8,ZDB-GENOX-141110-7");
        List<?> pubs = page.getByXPath("//a[@id='ZDB-PUB-081001-3']");
        assertEquals(1, pubs.size());
    }

    /**
     * Display Citation list for
     * Genotype + Morpholinos:  shha^tq252/tq252
     * with Publication: Phenotype Annotation (1994-2006)
     */
    @Test
    @Ignore
    public void testPhenotypeSummary() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/phenotype-summary?fishID=ZDB-GENO-980410-268,ZDB-GENOX-041102-2319&geneOrFeatureName=shha&mutationType=Select&anatomyTermIDs=&anatomyTermNames=&searchTerm=&filter1=showAll&sortBy=BEST_MATCH&maxDisplayRecords=20");
        List<?> pubs = page.getByXPath("//a[@id='ZDB-PUB-060503-2']");
        assertEquals(1, pubs.size());
    }

    /**
     * Check that the full expressed gene section is displayed looking for 'pax6a'
     */
    @Test
    @Ignore
    public void testExpressionSummary() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/fish-show-all-expression/ZDB-GENO-030619-2,ZDB-GENOX-070913-1,ZDB-GENOX-080917-1,ZDB-GENOX-141110-6");
        // make sure pax6a is listed
        List<?> pubs = page.getByXPath("//a[@id='ZDB-GENE-990415-200']");
        assertEquals(1, pubs.size());
    }

    /**
     * Check that the full expressed gene section is displayed looking for 'alcama'
     */
    @Test
    @Ignore
    public void testExpressionSummaryOnFishView() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/fish-detail/ZDB-GENO-030619-2,ZDB-GENOX-070913-1,ZDB-GENOX-080917-1,ZDB-GENOX-141110-6");
        // make sure alcama is listed
        List<?> pubs = page.getByXPath("//a[@id='ZDB-GENE-990415-30']");
        assertEquals(1, pubs.size());
    }

}
