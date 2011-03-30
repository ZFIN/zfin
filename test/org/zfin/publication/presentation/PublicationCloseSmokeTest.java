package org.zfin.publication.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

/**
 */
public class PublicationCloseSmokeTest extends AbstractSmokeTest {

    /**
     * Check that the publication close page is coming up.
     */
    @Test
    public void testClosePublicationPageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/publication/close-curation?publicationID=ZDB-PUB-990507-16");
                assertNotNull(page);

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

}
