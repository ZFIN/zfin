package org.zfin.fish.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class FishSmokeTest extends AbstractSmokeTest {


    public FishSmokeTest(WebClient webClient) {
        super(webClient);
    }

    /**
     * Just pull up the fish search form and check for no errors.
     */
    @Test
    public void testFishSearchPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/search");
        assertTrue(page.getTitleText().contains("ZFIN Fish Search"));
    }


    /**
     * Display Fish detail page for WT+MO1-shha
     */
    @Test
    public void testFishDetailPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/fish/ZDB-FISH-150901-19155");
        assertThat(page.getTitleText(), containsString("WT + MO1-shha"));
    }

    /**
     * Display Citation list for WT+MO1-shha
     */
    @Test
    public void testFishCitationList() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/publication/list/ZDB-FISH-150901-19155");
        List<?> pubs = page.getByXPath("//a[@id='ZDB-PUB-081001-3']");
        assertThat(pubs.size(), greaterThan(1));
    }

}
