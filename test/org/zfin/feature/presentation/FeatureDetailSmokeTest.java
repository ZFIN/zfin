package org.zfin.feature.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Ignore;
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
        assertEquals(page.getTitleText(), "ZFIN Feature: b191");
    }

}
