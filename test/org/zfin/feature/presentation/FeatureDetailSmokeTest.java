package org.zfin.feature.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class FeatureDetailSmokeTest extends AbstractSmokeTest {

    public FeatureDetailSmokeTest(WebClient webClient) {
        super();
        this.webClient = webClient;
    }


    @Test
    public void testFeaturePage() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-ALT-980203-401");
        assertEquals(page.getTitleText(),"Feature: b191");
        assertTrue("Should have several links in the genotype table", page.getElementById("feature-genotypes-table").getElementsByTagName("a").size() > 2);
    }

    @Test
    public void testFeaturePageWithOtherPagesLink() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-ALT-130627-1");
        assertEquals(page.getTitleText(),"Feature: tud11Gt");
        assertTrue("Should be at least one other feature page link", page.getElementById("other-feature-pages").getElementsByTagName("a").size() > 0);
    }

}
