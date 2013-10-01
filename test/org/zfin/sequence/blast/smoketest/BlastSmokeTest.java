package org.zfin.sequence.blast.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

@RunWith(Parameterized.class)
public class BlastSmokeTest extends AbstractSmokeTest {

    public BlastSmokeTest(WebClient webClient) {
        super(webClient);
    }

    /**
     * Check if the blast page is accessible
     */
    @Test
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
