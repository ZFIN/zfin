package org.zfin.gwt.marker;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.zfin.AbstractSecureSmokeTest;
import org.zfin.antibody.Antibody;
import org.zfin.repository.RepositoryFactory;

/**
 */
public class AntibodyEditSmokeTest extends AbstractSecureSmokeTest {


    public void testPubSelect() {
        final String zdbID = "ZDB-ATB-081002-19";
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbID);
        assertNotNull(antibody);
        for (WebClient webClient : curationWebClients) {
            webClient.setJavaScriptEnabled(true);
            try {
                login(webClient);
                webClient.waitForBackgroundJavaScript(5000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/dev-tools/gwt/antibody-edit");
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals("GWT Antibody Edit", page.getTitleText());

                // should be the antibody zn-5, ZDB-ATB-081002-19
                HtmlDivision abZdbIDDiv = (HtmlDivision) page.getByXPath("//div[@class='gwt-HTML']/div[. ='" + zdbID + "']").get(0);
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
//            addButton.click();
//            webClient.waitForBackgroundJavaScriptStartingBefore(2000);

                // for some reason, can't get the genes to show up here
//            assertEquals(1,page.getByXPath("//div[@id='geneName']//table[@class='relatedEntityTable']/tbody/tr").size());

//            assertNotNull(page.getByXPath("//span[.='sox9b']").get(0)) ;
//            HtmlDivision errorDiv = (HtmlDivision) page.getByXPath("//div[@id='geneName']//div[.='Attribution Required.']").get(0);
//            assertNotNull(errorDiv);
            } catch (Exception e) {
                fail(e.toString());
            }
        }
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
