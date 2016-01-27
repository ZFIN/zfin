package org.zfin.feature.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

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
        assertNotNull("Should have a genotype table", page.getElementById("genotypes-table"));
        assertTrue("Should have a non-empty collection of links in the genotype table",
                CollectionUtils.isNotEmpty(page.getElementById("genotypes-table").getElementsByTagName("a")));
        assertTrue("Should have several links in the genotype table", page.getElementById("genotypes-table").getElementsByTagName("a").size() > 2);
    }

    @Test
    public void testFeaturePageWithOtherPagesLink() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-ALT-130627-1");
        assertEquals(page.getTitleText(),"Feature: tud11Gt");
        assertTrue("Should be at least one other feature page link", page.getElementById("other_pages").getElementsByTagName("a").size() > 0);
    }

}
