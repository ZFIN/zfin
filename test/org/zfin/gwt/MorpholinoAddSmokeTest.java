package org.zfin.gwt;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.zfin.AbstractSecureSmokeTest;

import java.util.List;

/**
 * Test the Add Morpholino page: Make sure there is a Target Gene entry field that auto-completes on
 * genes and EFGs.
 */
public class MorpholinoAddSmokeTest extends AbstractSecureSmokeTest {

    private String pageUrl = "/webdriver?MIval=aa-new_marker.apg&marker_type=MRPHLNO&newmrkrSource=ZDB-PUB-990507-16";

    public void testTargetGeneInputFieldExists() {
        for (WebClient browser : publicWebClients) {
            browser.setJavaScriptEnabled(true);
            try {
                login(browser);
                browser.waitForBackgroundJavaScript(1000);
                HtmlPage page = browser.getPage(getApgNonSecureUrl() + pageUrl);
                browser.waitForBackgroundJavaScriptStartingBefore(3000);
                assertEquals("ZFIN Add Marker", page.getTitleText());

                // There should be an entry field that allows to enter a target gene.
                // this field is auto-completed.
                HtmlTextInput input = (HtmlTextInput) page.getByXPath("//input[@id='newmrkrTargetGene']").get(0);
                assertNotNull(input);
                List<?> divList = page.getByXPath("//div[@class='gwt-SuggestBoxPopup']");
                assertEquals(0, divList.size());

                // check that there is an auto-complete list for a gene string 'fgf*'
                input.type("fgf");
                browser.waitForBackgroundJavaScript(1000);
                assertEquals("fgf", input.getValueAttribute());
                browser.waitForBackgroundJavaScriptStartingBefore(4000);
                divList = page.getByXPath("//div[@class='gwt-SuggestBoxPopup']");
                assertNotNull(divList);
                assertEquals(1, divList.size());
                HtmlDivision div = (HtmlDivision)divList.get(0);
                assertNotNull(div);

                // check that there is no auto-complete list for a morpholino string 'MO1*'
                input.reset();
                input.type("MO1");
                browser.waitForBackgroundJavaScript(1000);
                assertEquals("MO1", input.getValueAttribute());
                browser.waitForBackgroundJavaScriptStartingBefore(4000);
                divList = page.getByXPath("//div[@class='gwt-SuggestBoxPopup']");
                assertNotNull(divList);
                assertEquals(0, divList.size());

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

}
