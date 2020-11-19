package org.zfin.mutant.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

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
    @Ignore("no need of the test?")
    public void testConstructSearchPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/search?q=&fq=category%3A%22Construct%22&category=Construct");
        assertTrue(page.getTitleText().contains("ZFIN Construct Search"));
    }

}
