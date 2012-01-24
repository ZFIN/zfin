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
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/anatomy-search");
                assertEquals("ZFIN Anatomy Search", page.getTitleText());
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
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/anatomy-do-search?searchTerm=emb*");
                assertEquals("ZFIN", page.getTitleText());
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
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/anatomy-view/ZDB-ANAT-010921-415");
                assertEquals("ZFIN Anatomy: brain", page.getTitleText());
                assertNotNull(page.getByXPath("//a[. = 'about']").get(0));
                assertNotNull(page.getByXPath("//a[. = 'PHENOTYPE']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // brain
    public void testShowAllMutantMorpholinos() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/show-all-morpholinos/ZDB-TERM-100331-8/false");
                assertTrue(page.getTitleText().startsWith("ZFIN"));
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENE-980526-362']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // neural tube
    public void testShowAllWTMorpholinos() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/show-all-morpholinos/ZDB-TERM-100331-1095/true");
                assertTrue(page.getTitleText().startsWith("ZFIN"));
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENE-030826-1']"));
                assertTrue(page.getByXPath("//a[@id = 'ZDB-GENE-030826-1']").size() > 0);
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENE-030826-1']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    //floor plate
    public void testShowAllMutants() {
        String genoID = "ZDB-GENO-050110-1";
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/anatomy/show-all-phenotype-mutants/ZDB-TERM-100331-21");
                assertTrue(page.getTitleText().startsWith("ZFIN"));
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENO-050110-1']"));
                assertTrue(page.getByXPath("//a[@id = 'ZDB-GENO-050110-1']").size() > 0);
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENO-050110-1']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


}