package org.zfin.gwt.lookup;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

@RunWith(Parameterized.class)
public class LookupSmokeTest extends AbstractSmokeTest {

    public LookupSmokeTest(WebClient webClient) throws Exception {
        super(webClient);
    }

    @Test
    public void testAnatomyLookupForm() {
             try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(secureUrlDomain + "/action/antibody/search");
                assertEquals("ZFIN Antibody Search", page.getTitleText());
                // this is here because the IE clients seems to be too slow otherwise
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
//                assertNotNull(page.getByXPath("//button[. = 'search']").get(0));
//                assertNotNull(page.getByXPath("//div[@class='gwt-Label']").get(0));
                HtmlInput htmlInput = (HtmlInput) page.getByXPath("//input[@id = 'searchTerm']").get(0);
                assertEquals("Should be an empty input term", "", htmlInput.getValueAttribute());
                htmlInput.setValueAttribute("pelv");
                assertEquals("pelv", htmlInput.getValueAttribute());
            } catch (Exception e) {
                fail("Client[" + webClient.getBrowserVersion().getApplicationName() + webClient.getBrowserVersion().getApplicationVersion() + "] failed with:\n" + e.toString());
            }
     }

    @Test
    public void testAnatomyLookupTyping() {
             try {
                HtmlPage page = webClient.getPage(secureUrlDomain + "/action/antibody/search");
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals("ZFIN Antibody Search", page.getTitleText());
                final HtmlForm form = page.getFormByName("Antibody Search");
                final HtmlTextInput textField = form.getInputByName("searchTerm");

                assertEquals("", textField.getValueAttribute());
                textField.type("pelv");
                webClient.waitForBackgroundJavaScript(2000);

                assertEquals("pelv", textField.getValueAttribute());

                webClient.waitForBackgroundJavaScriptStartingBefore(2000);

                assertNotNull(page.getByXPath("//div[@class='gwt-SuggestBoxPopup']"));
                assertTrue(page.getByXPath("//td[@class='item']").size() > 4);
                HtmlTableDataCell cell = ((HtmlTableDataCell) page.getByXPath("//td[@class='item']").get(4)) ;
                String cellString = cell.getTextContent() ;
                assertTrue(cellString.contains("pelv"));

                // the very first element is selected by default
                assertEquals(1, page.getByXPath("//td[@class='item item-selected']").size());
                assertNotSame("...", ((HtmlTableDataCell) page.getByXPath("//td[@class='item']").get(3)).getTextContent());

            } catch (Exception e) {
                fail(e.toString());
            }
     }

}
