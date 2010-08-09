package org.zfin.anatomy;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.zfin.AbstractSecureSmokeTest;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

/**
 */
public class AnatomySmokeTest extends AbstractSecureSmokeTest {

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

    public void testAnatomyLookupForm() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                login();
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(ZfinPropertiesEnum.NON_SECURE_HTTP + domain + "/action/dev-tools/gwt/lookup-table");
                assertEquals("GWT Lookup Table", page.getTitleText());
                // this is here because the IE clients seems to be too slow otherwise
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertNotNull(page.getByXPath("//button[. = 'search']").get(0));
                assertNotNull(page.getByXPath("//td[. = 'Enter search terms']").get(0));
                assertEquals("Enter search terms", ((HtmlTableDataCell) page.getByXPath("//td[. = 'Enter search terms']").get(0)).getTextContent());
                assertNotNull(page.getByXPath("//div[@class='gwt-Label']").get(0));
                HtmlInput htmlInput = (HtmlInput) page.getByXPath("//input[@id = 'searchTerm']").get(0);
                assertEquals("Should be an empty input term","", htmlInput.getValueAttribute());
                htmlInput.setValueAttribute("pelv");
                assertEquals("pelv", htmlInput.getValueAttribute());
            } catch (Exception e) {
                fail("Client["+webClient.getBrowserVersion().getApplicationName()+ webClient.getBrowserVersion().getApplicationVersion()+"] failed with:\n" + e.toString());
            }
        }
    }



    public void testAnatomyLookupTyping() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                login();
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(ZfinPropertiesEnum.NON_SECURE_HTTP + domain + "/action/dev-tools/gwt/lookup-table");
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals("GWT Lookup Table", page.getTitleText());
                final HtmlForm form = page.getFormByName("lookupTable");
                final HtmlTextInput textField = form.getInputByName("searchTerm");

                assertEquals("Lookup Table Application", ((HtmlHeading1) page.getByXPath("//h1").get(0)).getTextContent());
                assertEquals("", textField.getValueAttribute());

                textField.type("pelv");
                webClient.waitForBackgroundJavaScript(2000);

                assertEquals("pelv", textField.getValueAttribute());

                webClient.waitForBackgroundJavaScriptStartingBefore(2000);

                assertNotNull(page.getByXPath("//div[@class='gwt-SuggestBoxPopup']"));
                assertEquals(20, page.getByXPath("//span[@class='autocomplete-plain']").size());
                assertTrue(((HtmlSpan) page.getByXPath("//span[@class='autocomplete-plain']").get(19)).getTextContent().contains("pelv"));

                // the very first element is selected by default
                assertEquals(1, page.getByXPath("//td[@class='item item-selected']").size());
                assertEquals(19, page.getByXPath("//td[@class='item']").size());
                assertNotSame("...", ((HtmlTableDataCell) page.getByXPath("//td[@class='item']").get(18)).getTextContent());

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


}