package org.zfin.gwt;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSecureSmokeTest;

import java.util.List;

/**
 * Test the Add Morpholino page: Make sure there is a Target Gene entry field that auto-completes on
 * genes and EFGs.
 */
@RunWith(Parameterized.class)
public class MorpholinoAddSmokeTest extends AbstractSecureSmokeTest {

    public MorpholinoAddSmokeTest(WebClient webClient) {
        super(webClient);
    }

    private String pageUrl = "?MIval=aa-new_marker.apg&marker_type=MRPHLNO&newmrkrSource=ZDB-PUB-990507-16";

    @Test
    public void testTargetGeneInputFieldExists() throws Exception {
        login(webClient);
        webClient.waitForBackgroundJavaScript(1000);
        HtmlPage page = webClient.getPage(getApgNonSecureUrl() + pageUrl);
        webClient.waitForBackgroundJavaScriptStartingBefore(3000);
        assertEquals("ZFIN Add Marker", page.getTitleText());

        // There should be an entry field that allows to enter a target gene.
        // this field is auto-completed.
        HtmlTextInput input = (HtmlTextInput) page.getByXPath("//input[@id='newmrkrTargetGene']").get(0);
        assertNotNull(input);
        List<?> divList = page.getByXPath("//div[@class='gwt-SuggestBoxPopup']");
        assertEquals(0, divList.size());

        // check that there is an auto-complete list for a gene string 'fgf*'
        input.type("fgf");
        webClient.waitForBackgroundJavaScript(1000);
        assertEquals("fgf", input.getValueAttribute());
        webClient.waitForBackgroundJavaScriptStartingBefore(4000);
        divList = page.getByXPath("//div[@class='gwt-SuggestBoxPopup']");
        assertNotNull(divList);
        assertEquals(1, divList.size());
        HtmlDivision div = (HtmlDivision) divList.get(0);
        assertNotNull(div);

        // check that there is no auto-complete list for a morpholino string 'MO1*'
        input.reset();
        input.type("MO1");
        webClient.waitForBackgroundJavaScript(1000);
        assertEquals("MO1", input.getValueAttribute());
        webClient.waitForBackgroundJavaScriptStartingBefore(4000);
        divList = page.getByXPath("//div[@class='gwt-SuggestBoxPopup']");
        assertNotNull(divList);
        assertEquals(0, divList.size());
    }

}
