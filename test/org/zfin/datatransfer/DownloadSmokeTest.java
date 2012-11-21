package org.zfin.datatransfer;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

/**
 */
public class DownloadSmokeTest extends AbstractSmokeTest {

    /**
     * Check that the download page is coming up
     */
    public void testMainDownloadPage() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads");
                assertTrue("Download Archive", page.getTitleText().contains("Download Archive"));

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Check that site search index summary page is coming up
     */
    public void testDeveloperDownloadPage() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads/archive");
                assertTrue("Download Archive", page.getTitleText().contains("Download Archive"));

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

}
