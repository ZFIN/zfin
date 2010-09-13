package org.zfin.gwt;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang.StringUtils;
import org.zfin.AbstractSecureSmokeTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class SimpleSmokeTest extends AbstractSecureSmokeTest {

    public void testSimpleComposite() {
        for (WebClient webClient : publicWebClients) {
            try {
                login(webClient);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/dev-tools/gwt/modules");
                webClient.setJavaScriptEnabled(true);
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals("GWT Modules", page.getTitleText());
                HtmlAnchor htmlAnchor = (HtmlAnchor) page.getByXPath("//a[ . = 'Test: TestComposite']").get(0);
                assertNotNull(htmlAnchor);
                page = htmlAnchor.click();
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlButton htmlButton1 = (HtmlButton) page.getByXPath("//button[. = 'b1']").get(0);
                assertNotNull(htmlButton1);
                HtmlButton htmlButton2 = (HtmlButton) page.getByXPath("//button[. = 'b2']").get(0);
                assertNotNull(htmlButton2);
                HtmlButton htmlButton3 = (HtmlButton) page.getByXPath("//button[. = 'b3']").get(0);
                assertNotNull(htmlButton3);
                HtmlDivision htmlDivision = (HtmlDivision) page.getByXPath("//div[@class='gwt-Label']").get(0);
                assertNotNull(htmlDivision);
                assertEquals("dogz", htmlDivision.getTextContent());
                htmlButton1.click();
                assertEquals("catz", htmlDivision.getTextContent());
                htmlButton2.click();
                assertEquals("dogz", htmlDivision.getTextContent());

                webClient.setAlertHandler(new AlertHandler() {
                    @Override
                    public void handleAlert(Page page, String s) {
                        assertEquals("red alert", s);
                    }
                });
                htmlButton3.click();
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    public void testAlternateGeneNote() {
        for (WebClient webClient : curationWebClients) {
            try {
                login(webClient);
                // first we have to guarantee that we always have a note there
                Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-001103-2");
                String oldNote = gene.getPublicComments();
                if (StringUtils.isEmpty(oldNote)) {
                    HibernateUtil.createTransaction();
                    gene.setPublicComments("new note");
                    HibernateUtil.flushAndCommitCurrentSession();
                }

//            final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/dev-tools/gwt/alternate-gene-edit");
                webClient.setJavaScriptEnabled(true);
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals("GWT Gene Edit", page.getTitleText());
                final HtmlDivision div = (HtmlDivision) page.getByXPath("//div[@id='curatorNoteName']").get(0);
                assertNotNull(div);
                final HtmlTable table = (HtmlTable) page.getByXPath("//table[@class='gwt-editbox']").get(0);
                assertNotNull(table);
                final HtmlTableDataCell cell = (HtmlTableDataCell) page.getByXPath("//td[. = 'PUBLIC']").get(0);
                assertNotNull(cell);
                final HtmlTextArea textArea = (HtmlTextArea) page.getByXPath("//td[. = 'PUBLIC']/..//textarea").get(0);
                assertNotNull(textArea);

                final HtmlButton saveButton = (HtmlButton) page.getByXPath("//td[. = 'PUBLIC']/..//button[. = 'Save']").get(0);
                assertNotNull(saveButton);

                final HtmlButton revertButton = (HtmlButton) page.getByXPath("//td[. = 'PUBLIC']/..//button[. = 'Revert']").get(0);
                assertNotNull(revertButton);

                // init satate
                assertTrue(saveButton.isDisabled());
                assertTrue(saveButton.isDisabled());

                String oldText = textArea.getText();
                String newText = "very unique updated text super uniqueness";

                // put in new text
                textArea.setText(newText);
                assertFalse(saveButton.isDisabled());
                assertFalse(revertButton.isDisabled());
                assertEquals(newText, textArea.getText());

                // revert
                revertButton.click();
                assertEquals(oldText, textArea.getText());
                assertTrue(saveButton.isDisabled());
                assertTrue(saveButton.isDisabled());

                // try saving this time
                textArea.setText(newText);
                assertFalse(saveButton.isDisabled());
                assertFalse(revertButton.isDisabled());
                saveButton.click();
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals(newText, textArea.getText());
                assertTrue(saveButton.isDisabled());
                assertTrue(saveButton.isDisabled());


                // put back
                textArea.setText(oldText);
                saveButton.click();
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                assertEquals(oldText, textArea.getText());

                if (StringUtils.isEmpty(oldNote)) {
                    HibernateUtil.createTransaction();
                    gene.setPublicComments(oldNote);
                    HibernateUtil.flushAndCommitCurrentSession();
                }
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

}
