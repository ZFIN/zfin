package org.zfin.sequence.blast.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

/**
 */
public class BlastSmokeTest extends AbstractSmokeTest {

    /**
     * Check if the blast page is accessible
     */
    public void testBlastPageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/blast/blast");
                assertEquals("ZFIN: BLAST Query", "ZFIN: BLAST Query", page.getTitleText());

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

}
