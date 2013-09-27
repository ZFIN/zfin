package org.zfin.expression;


import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.junit.Test;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

public class FigureViewSmokeTest extends AbstractSmokeTest {
    /**
    * Checks that the title text, gene (myod1), and footer text are all properly loaded for Figure 1 of ZDB-PUB-071210-28
    */
    @Test
    public void testFigureOneView() {
        for (WebClient webClient : getBrowserClients()) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-FIG-080508-24");
                assertEquals("ZFIN: Figure: Ochi et al., 2008, Fig. 1", page.getTitleText());
                assertNotNull("No Title Found", page.getByXPath("//span[@title = 'myogenic differentiation 1']"));
                assertNotNull("No Footer Found", page.getByXPath("//div[@id = 'footer']"));
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }


    /**
     * Checks title, gene (smarcd3b), and footer text are all properly loaded for Figure S2 of ZDB-PUB-071210-28
     */
    @Test
    public void testFigureSuppTwoView() {
        for (WebClient webClient : getBrowserClients()) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-FIG-080521-3");
                assertEquals("ZFIN: Figure: Ochi et al., 2008, Fig. S2", page.getTitleText());
                assertNotNull(page.getByXPath("//span[@title = 'SWI/SNF related, matrix associated, actin dependent regulator of chromatin, subfamily d, member 3b']"));
                assertNotNull(page.getByXPath("//div[@id = 'footer']"));
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Checks that the title text and footer are all properly loaded for the detailed pub view of ZDB-PUB-071210-28
     */
    @Test
    public void testDetailedPubView() {
        for (WebClient webClient : getBrowserClients()) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-PUB-071210-28");
                assertEquals("ZFIN: Publication: Ochi et al., 2008. Smarcd3 regulates the timing of zebrafish myogenesis onset", page.getTitleText());
                assertNotNull(page.getByXPath("//div[@id = 'footercredits']"));
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Checks that the title text, genes, and footer are properly loaded for the view of all figures of ZDB-PUB-071210-28
     */
    @Test
    public void testAllFiguresView() {
        for (WebClient webClient : getBrowserClients()) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/cgi-bin/webdriver?MIval=aa-fxallfigures.apg&OID=ZDB-PUB-071210-28");
                assertEquals("ZFIN View Publication:ZDB-PUB-071210-28", page.getTitleText());
                List<HtmlSpan> genes = (List<HtmlSpan>) page.getByXPath("//span[@class = 'genedom']");
                assertNotNull(genes);
                assertNotNull(page.getByXPath("//div[@id = 'footercredits']"));
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }
}
