package org.zfin.fish.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import org.junit.Test;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

/**
 */
public class FishSmokeTest extends AbstractSmokeTest {

    /**
     * Just pull up the fish search form and check for no errors.
     */
    @Test
    public void testFishSearchPageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/search");
                assertTrue(page.getTitleText().contains("ZFIN Fish Search"));

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }


    /**
     * Display Fish detail page for
     * Genotype + Morpholinos:  wild type (unspecified)+MO1-shha
     */
    @Test
    public void testFishDetailPageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/fish-detail/ZDB-GENO-030619-2,ZDB-GENOX-090731-5");
                assertTrue(page.getTitleText().contains("ZFIN Fish: WT"));

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Display Citation list for
     * Genotype + Morpholinos:  wild type (unspecified)+MO1-shha
     */
    @Test
    public void testFishCitationList() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/fish-publication-list?fishID=ZDB-GENO-030619-2,ZDB-GENOX-090731-5");
                List<?> pubs = page.getByXPath("//a[@id='ZDB-PUB-081001-3']");
                assertEquals(1, pubs.size());

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Display Citation list for
     * Genotype + Morpholinos:  shha^tq252/tq252
     * with Publication: Phenotype Annotation (1994-2006)
     */
    @Test
    public void testPhenotypeSummary() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/phenotype-summary?fishID=ZDB-GENO-980410-268,ZDB-GENOX-041102-2319&geneOrFeatureName=shha&mutationType=Select&anatomyTermIDs=&anatomyTermNames=&searchTerm=&filter1=showAll&sortBy=BEST_MATCH&maxDisplayRecords=20");
                List<?> pubs = page.getByXPath("//a[@id='ZDB-PUB-060503-2']");
                assertEquals(1, pubs.size());

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

}
