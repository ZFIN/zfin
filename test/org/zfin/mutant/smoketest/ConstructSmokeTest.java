package org.zfin.mutant.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class ConstructSmokeTest extends AbstractSmokeTest {

    public ConstructSmokeTest(WebClient webClient) {
        super();
        this.webClient = webClient;
    }

    /**
     * Just pull up the fish search form and check for no errors.
     */
    @Test
    public void testConstructSearchPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/search?q=&fq=category%3A%22Construct%22&category=Construct");
        assertTrue(page.getTitleText().contains("ZFIN Construct Search"));
    }


    /**
     * Display Construct detail page

     */


    /**
     * Display Citation list for
     * Genotype + Morpholinos:  shha^tq252/tq252
     * with Publication: Phenotype Annotation (1994-2006)
     */
    @Test
    public void testExpressionSummary() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/mutant/construct-expression-summary?constructID=ZDB-ETCONSTRCT-080529-1&construct=uas&allTypes=allConstructs&_allTypes=on&maxDisplayRecords=&page=1&promoterOfGene=&_allTg=on&drivesExpressionOfGene=&_allEt=on&affectedGene=&_allGt=on&anatomyTermIDs=&anatomyTermNames=&searchTerm=&_allPt=on");
        List<?> pubs = page.getByXPath("//a[@id='ZDB-PUB-120306-5']");
        assertEquals(1, pubs.size());
    }

    /**
     * Check that the full expressed gene section is displayed looking for 'pax6a'
     */

    /**
     * Check that the full expressed gene section is displayed looking for 'alcama'
     */


}
