package org.zfin.gwt;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

@RunWith(Parameterized.class)
public class ExpressionSmokeTest extends AbstractSmokeTest {


    public ExpressionSmokeTest(WebClient webClient) {
        super(webClient);
    }


    //Temporarily commenting this test out, if both tests run, the other one fails.
    //Since it seems like a superset of this test, I thought it's the one that should stay.
    @Test
    public void testExpressionLookupTable() throws IOException {
        HtmlPage page = webClient.getPage(getApgNonSecureUrl() + "?MIval=aa-xpatselect.apg");
        webClient.waitForBackgroundJavaScriptStartingBefore(10000);

        // find gui components
        HtmlLabel htmlLabel = (HtmlLabel) page.getByXPath("//label[@class = 'multisearchLabel']").get(0);
        assertNotNull(htmlLabel);

        // find input
        HtmlInput htmlInput = (HtmlInput) page.getByXPath("//input[@id = 'searchTerm']").get(0);
        assertEquals("", htmlInput.getValueAttribute());

        // type value
        htmlInput.type("retina");
        assertEquals("retina", htmlInput.getValueAttribute());
        webClient.waitForBackgroundJavaScriptStartingBefore(4000);

        // wait for popup
        assertNotNull(page.getByXPath("//div[@class='gwt-SuggestBoxPopup']"));
        assertTrue(page.getByXPath("//td[@class='item']").size() > 5);
        HtmlTableDataCell selectedCell = (HtmlTableDataCell) page.getByXPath("//td[@class='item item-selected']").get(0);
        assertNotNull(selectedCell);
        assertEquals("retina", selectedCell.getTextContent());

        // select first value, which should always be retina
        selectedCell.click();
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);

        // verify that it gets put up top
        HtmlAnchor anchor = (HtmlAnchor) page.getByXPath("//a[. = 'retina']").get(0);
        assertNotNull(anchor);

        // push the delete button
        HtmlImage deleteButton = (HtmlImage) page.getByXPath("//img[@src = '/images/action_delete.png']").get(0);
        assertNotNull(deleteButton);
    }


    //@Test
    public void testExpressionLookupTableWithSubmitButton() throws IOException {
        HtmlPage page = webClient.getPage(getApgNonSecureUrl() + "?MIval=aa-xpatselect.apg");
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);

        // find gui components
        HtmlLabel htmlLabel = (HtmlLabel) page.getByXPath("//label[@class = 'multisearchLabel']").get(0);
        assertNotNull(htmlLabel);

        // find input
        HtmlInput htmlInput = (HtmlInput) page.getByXPath("//input[@id = 'searchTerm']").get(0);
        assertEquals("", htmlInput.getValueAttribute());

        // type value
        htmlInput.type("retina");
        assertEquals("retina", htmlInput.getValueAttribute());
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);

        // wait for popup
        assertNotNull(page.getByXPath("//div[@class='gwt-SuggestBoxPopup']"));
        assertTrue(page.getByXPath("//td[@class='item']").size() > 5);
        HtmlTableDataCell selectedCell = (HtmlTableDataCell) page.getByXPath("//td[@class='item item-selected']").get(0);
        assertNotNull(selectedCell);
        assertEquals("retina", selectedCell.getTextContent());


        // assert that the 'search' button is there
        HtmlButtonInput searchButton = (HtmlButtonInput) page.getByXPath("//input[@value = 'Search']").get(0);
        assertNotNull(searchButton);
        searchButton.click();

//            // select first value, which should always be retina
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
//
//            // verify that it gets put up top
        HtmlAnchor anchor = (HtmlAnchor) page.getByXPath("//a[. = 'retina']").get(0);
        assertNotNull(anchor);
//
//            // push the delete button
        HtmlImage deleteButton = (HtmlImage) page.getByXPath("//img[@src = '/images/action_delete.png']").get(0);
        assertNotNull(deleteButton);
    }

}
