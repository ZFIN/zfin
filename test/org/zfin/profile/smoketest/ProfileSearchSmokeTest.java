package org.zfin.profile.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class ProfileSearchSmokeTest extends AbstractSmokeTest {

    public ProfileSearchSmokeTest(WebClient webClient) {
        super(webClient);
    }

    @Test
    public void testLabSearchReturnsResults() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain +
            "/action/profile/lab/search/execute?name=zfin&maxDisplayRecords=25&containsType=bio&action=Search");
        assertThat(page.getTitleText(), containsString("Lab Search"));
        assertThat(page.getByXPath("//table[contains(@class,'searchresults')]").size(), greaterThan(0));
    }

    @Test
    public void testCompanySearchReturnsResults() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain +
            "/action/profile/company/search/execute?name=aquatic&maxDisplayRecords=25&containsType=bio&action=Search");
        assertThat(page.getTitleText(), containsString("Company Search"));
        assertThat(page.getByXPath("//table[contains(@class,'searchresults')]").size(), greaterThan(0));
    }

    @Test
    public void testPersonSearchReturnsResults() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain +
            "/action/profile/person/search/execute?name=westerfield&maxDisplayRecords=25&containsType=bio&action=Search");
        assertThat(page.getTitleText(), containsString("Person Search"));
        assertThat(page.getByXPath("//table[contains(@class,'searchresults')]").size(), greaterThan(0));
    }

}
