package org.zfin.gwt.marker;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSecureSmokeTest;
import org.zfin.antibody.Antibody;
import org.zfin.repository.RepositoryFactory;

import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class AntibodyEditSmokeTest extends AbstractSecureSmokeTest {

    public AntibodyEditSmokeTest(WebClient webClient) throws Exception {
        super(webClient);
    }

    @Test
    public void testAntibodyEditPage() throws Exception {
        final String zdbID = "ZDB-ATB-081002-19";
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
        assertNotNull(antibody);
        webClient.waitForBackgroundJavaScript(5000);
        HtmlPage page = webClient.getPage(secureUrlDomain + "/action/marker/marker-edit?zdbID=" + zdbID);
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        assertEquals("ZFIN Edit Antibody: zn-5", page.getTitleText());

        // should be the antibody zn-5, ZDB-ATB-081002-19
        List<?> xPath = page.getByXPath("//div[@class='gwt-HTML']/div[. ='" + zdbID + "']");
        checkCollectionNotNull(xPath, "ZDB ID");
        HtmlDivision abZdbIDDiv = (HtmlDivision) xPath.get(0);
        assertNotNull(abZdbIDDiv);

        HtmlBold antigenTitle = (HtmlBold) page.getByXPath("//b[. ='Antigen Genes:']").get(0);
        assertNotNull(antigenTitle);
        HtmlDivision geneNameDiv = (HtmlDivision) page.getByXPath("//div[@id='geneName']").get(0);
        assertNotNull(geneNameDiv);
        HtmlTextInput input = (HtmlTextInput) geneNameDiv.getByXPath("//div[@id='supplierName']//input[@class='gwt-SuggestBox']").get(0);
        assertNotNull(input);
        assertEquals("", input.getValueAttribute());
        input.setValueAttribute("sox9b");
        assertEquals("sox9b", input.getValueAttribute());

        HtmlTextInput pubInput = (HtmlTextInput) page.getByXPath("//div[@id='publicationName']//input[@class='gwt-TextBox']").get(0);
        assertNotNull(pubInput);

        String pubID = "ZDB-PUB-080117-1";
        HtmlOption pubSelect = (HtmlOption) page.getByXPath("//div[@id='publicationName']//option[@value='" + pubID + "']").get(0);
        assertNotNull(pubSelect);
        pubSelect.setSelected(true);
        webClient.waitForBackgroundJavaScript(2000);

        HtmlDivision genePubDiv = (HtmlDivision) page.getByXPath("//div[@id='geneName']//div[@class='relatedEntityDefaultPub']").get(0);
        assertNotNull(genePubDiv);
        assertEquals(pubID, genePubDiv.getTextContent());

        assertEquals("ZDB-PUB-080117-1", pubInput.getValueAttribute());

        HtmlButton addButton = (HtmlButton) page.getByXPath("//div[@id='geneName']//button[@class='gwt-Button']").get(0);
        assertNotNull(addButton);
    }

    public void checkCollectionNotNull(Collection collection, String name) {
        if (CollectionUtils.isEmpty(collection))
            fail("No " + name + "found on page");
    }

//    public void testHandleAlias(){
//
//    }
//
//    public void testAntibodyData(){
//
//    }
//
//    public void testAddExternalNotes(){
//
//    }
//
//    public void testAddSuppliers(){
//
//    }
//
//    public void testHandleAttributions(){
//
//    }

}
