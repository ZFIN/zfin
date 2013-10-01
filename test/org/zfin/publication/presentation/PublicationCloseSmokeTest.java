package org.zfin.publication.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

@RunWith(Parameterized.class)
public class PublicationCloseSmokeTest extends AbstractSmokeTest {

    public PublicationCloseSmokeTest(WebClient webClient) {
        super();
        this.webClient = webClient;
    }

    /**
     * Check that the publication close page is coming up.
     */
    @Test
    public void testClosePublicationPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/publication/close-curation?publicationID=ZDB-PUB-990507-16");
        assertNotNull(page);
    }

}
