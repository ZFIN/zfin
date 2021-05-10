package org.zfin.datatransfer;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

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
        assertThat("Page title contains \"Downloads Archive\"", page.getTitleText(), containsString("Downloads Archive"));
    }

    /**
     * Check that site search index summary page is coming up
     */
    @Test
    public void testDeveloperDownloadPage() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads/archive");
        assertThat("Page title contains \"Downloads Archive\"", page.getTitleText(), containsString("Downloads Archive"));
    }

    /**
     * Just pull up the most current downloads page
     */
    @Test
    public void testDownloadPagePageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/unload/downloads");
                assertThat("Page title contains \"Downloads Archive:\"",  page.getTitleText(), containsString("Downloads Archive:"));
                List<?> downloadFileLink = page.getByXPath("//a[@id='antibodies.txt']");
                // exactly one link
                assertThat("antibodies.txt download should exist", downloadFileLink, notNullValue());
                assertThat("one link to antibodies.txt should exist", downloadFileLink, hasSize(1));
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }


}
