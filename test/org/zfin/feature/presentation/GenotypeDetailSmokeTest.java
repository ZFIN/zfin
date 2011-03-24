package org.zfin.feature.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.zfin.AbstractSmokeTest;

/**
 */
public class GenotypeDetailSmokeTest extends AbstractSmokeTest {

    private String pageUrl = "/action/genotype/genotype-detail?zdbID=ZDB-GENO-050809-10";

    public void testGenotypeDetailPage() {
        for (WebClient browser : curationWebClients) {
            browser.setJavaScriptEnabled(true);
            try {
                browser.waitForBackgroundJavaScript(2000);
                HtmlPage page = browser.getPage("http://" + domain + pageUrl);
                browser.waitForBackgroundJavaScriptStartingBefore(1000);
                assertEquals("Genotype: Tg(myl7:EGFP)twu34", page.getTitleText());

                // There should a gene in the Gene expression section with abbreviation 'alcama'
                HtmlSpan htmlSpan = (HtmlSpan) page.getByXPath("//span[@title='activated leukocyte cell adhesion molecule a']").get(0);
                assertNotNull(htmlSpan);
                assertEquals("alcama", htmlSpan.getTextContent());

                // There should a morpholino in the Phenotype section with abbreviation 'MO4-foxn4'
                htmlSpan = (HtmlSpan) page.getByXPath("//span[@title='MO1-foxn4']").get(0);
                assertNotNull(htmlSpan);
                assertEquals("MO1-foxn4", htmlSpan.getTextContent());

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

}