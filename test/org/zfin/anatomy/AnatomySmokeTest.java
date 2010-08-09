package org.zfin.anatomy;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.zfin.AbstractSmokeTest;
import org.zfin.properties.ZfinPropertiesEnum;

/**
 */
public class AnatomySmokeTest extends AbstractSmokeTest {

    public void testAnatomyLookupFormExists() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(ZfinPropertiesEnum.NON_SECURE_HTTP + domain + "/action/anatomy/search");
                assertEquals("Anatomical Ontology Browser", page.getTitleText());
                assertNotNull(page.getByXPath("//label[. = 'Anatomical Term']").get(0));
                HtmlInput htmlInput = (HtmlInput) page.getByXPath("//input[@id = 'searchTerm']").get(0);
                assertEquals("", htmlInput.getValueAttribute());
                htmlInput.setValueAttribute("pelv");
                assertEquals("pelv", htmlInput.getValueAttribute());
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


    public void testAnatomyDetailPage() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(ZfinPropertiesEnum.NON_SECURE_HTTP + domain + "/action/anatomy/term-detail?anatomyItem.zdbID=ZDB-ANAT-010921-415");
                assertEquals("ZFIN: Anatomical Structure: brain", page.getTitleText());
                assertNotNull(page.getByXPath("//a[. = 'about']").get(0)) ;
                assertNotNull(page.getByXPath("//a[. = 'PHENOTYPE']").get(0)) ;
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


}