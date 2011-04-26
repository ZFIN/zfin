package org.zfin.anatomy;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.zfin.AbstractSmokeTest;

import java.util.List;

/**
 */
public class AnatomySmokeTest extends AbstractSmokeTest {

    public void testAnatomyLookupFormExists() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/search");
                assertEquals("Anatomical Ontology Browser", page.getTitleText());
                assertNotNull(page.getByXPath("//label[. = 'Anatomical Term']").get(0));
                HtmlInput htmlInput = (HtmlInput) page.getByXPath("//input[@id = 'searchTerm']").get(0);
                assertEquals("", htmlInput.getValueAttribute());
                htmlInput.setValueAttribute("pelv");
                assertEquals("pelv", htmlInput.getValueAttribute());
                List<FrameWindow> iFrames = page.getFrames();
                assertNotNull(iFrames);
                assertEquals(1, iFrames.size());
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    public void testAnatomySearchMultipleResults() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/search?action=term-search&searchTerm=emb*");
                assertEquals("Anatomical Ontology Browser", page.getTitleText());
                List<HtmlSpan> caption = (List<HtmlSpan>) page.getByXPath("//caption[@id = 'Results for emb search']");
                assertNotNull(caption);
                assertEquals(1, caption.size());
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


    public void testAnatomyDetailPage() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/term-detail?anatomyItem.zdbID=ZDB-ANAT-010921-415");
                assertEquals("ZFIN: Anatomical Structure: brain", page.getTitleText());
                assertNotNull(page.getByXPath("//a[. = 'about']").get(0));
                assertNotNull(page.getByXPath("//a[. = 'PHENOTYPE']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


}