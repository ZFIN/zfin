package org.zfin.datatransfer;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class DownloadSmokeTest extends AbstractSmokeTest {

    public DownloadSmokeTest(WebClient webClient) {
        super(webClient);
    }

    /**
     * Check that the download page is coming up
     */
    @Test
    public void testMainDownloadPage() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads");
        assertTrue("Downloads Archive", page.getTitleText().contains("Download Archive"));
    }

    /**
     * Check that site search index summary page is coming up
     */
    @Test
    public void testDeveloperDownloadPage() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads/archive");
        assertTrue("Downloads Archive", page.getTitleText().contains("Download Archive"));
    }

    /**
     * Just pull up the most current downloads page
     */
    @Test
    public void testDownloadPagePageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads");
                assertTrue("Archive:",  page.getTitleText().startsWith("Download Archive:"));
                List<?> downloadFileLink = page.getByXPath("//a[@id='antibodies.txt']");
                // exactly one link
                assertNotNull("could not find antibodies.txt download link", downloadFileLink);
                assertEquals(1, downloadFileLink.size());
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }


}
