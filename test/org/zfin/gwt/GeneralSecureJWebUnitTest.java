package org.zfin.gwt;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang.StringUtils;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;

/**
 * This class uses the more raw HtmlUnit protocols.
 */
public class GeneralSecureJWebUnitTest extends AbstractSecureJWebUnitTest {

    public void testSimpleComposite(){
        try {
            login();
            HtmlPage page = webClient.getPage(ZfinProperties.NON_SECURE_HTTP+domain +"/action/dev-tools/gwt/modules");
            webClient.setJavaScriptEnabled(true);
            webClient.waitForBackgroundJavaScriptStartingBefore(2000);
            assertEquals("GWT Modules",page.getTitleText());
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
            assertEquals("dogz",htmlDivision.getTextContent());
            htmlButton1.click();
            assertEquals("catz",htmlDivision.getTextContent());
            htmlButton2.click();
            assertEquals("dogz",htmlDivision.getTextContent());

            webClient.setAlertHandler(new AlertHandler(){
                @Override
                public void handleAlert(Page page, String s) {
                    assertEquals("red alert",s);
                }
            });
            htmlButton3.click();
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testAlternateGeneNote(){

        try {
            login();
            // first we have to guarantee that we always have a note there
            Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-001103-2") ;
            String oldNote = gene.getPublicComments();
            if(StringUtils.isEmpty(oldNote)){
                HibernateUtil.createTransaction();
                gene.setPublicComments("new note");
                HibernateUtil.flushAndCommitCurrentSession();
            }

//            final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
            HtmlPage page = webClient.getPage(ZfinProperties.NON_SECURE_HTTP+domain +"/action/dev-tools/gwt/alternate-gene-edit");
            webClient.setJavaScriptEnabled(true);
            webClient.waitForBackgroundJavaScriptStartingBefore(2000) ;
            assertEquals("GWT Gene Edit",page.getTitleText());
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
            assertTrue(saveButton.isDisabled()) ;
            assertTrue(saveButton.isDisabled()) ;

            String oldText= textArea.getText();
            String newText =  "very unique updated text super uniqueness";

            // put in new text
            textArea.setText(newText);
            assertFalse(saveButton.isDisabled()) ;
            assertFalse(revertButton.isDisabled()) ;
            assertEquals(newText,textArea.getText()) ;

            // revert
            revertButton.click();
            assertEquals(oldText,textArea.getText()) ;
            assertTrue(saveButton.isDisabled()) ;
            assertTrue(saveButton.isDisabled()) ;

            // try saving this time
            textArea.setText(newText);
            assertFalse(saveButton.isDisabled()) ;
            assertFalse(revertButton.isDisabled()) ;
            saveButton.click();
            webClient.waitForBackgroundJavaScriptStartingBefore(2000) ;
            assertEquals(newText,textArea.getText()) ;
            assertTrue(saveButton.isDisabled()) ;
            assertTrue(saveButton.isDisabled()) ;


            // put back
            textArea.setText(oldText);
            saveButton.click();
            webClient.waitForBackgroundJavaScriptStartingBefore(2000) ;
            assertEquals(oldText,textArea.getText()) ;

            if(StringUtils.isEmpty(oldNote)){
                HibernateUtil.createTransaction();
                gene.setPublicComments(oldNote);
                HibernateUtil.flushAndCommitCurrentSession();
            }


        } catch (Exception e) {
            fail(e.toString());
        }

    }

    public void testAnatomyLookupForm(){

        try {
            login();
            webClient.waitForBackgroundJavaScriptStartingBefore(2000);
            HtmlPage page = webClient.getPage(ZfinProperties.NON_SECURE_HTTP+domain +"/action/dev-tools/gwt/lookup-table");
            assertEquals("GWT Lookup Table",page.getTitleText());
            assertNotNull(page.getByXPath("//button[. = 'search']").get(0));
            assertNotNull(page.getByXPath("//td[. = 'Enter search terms']").get(0));
            assertEquals( "Enter search terms",((HtmlTableDataCell) page.getByXPath("//td[. = 'Enter search terms']").get(0)).getTextContent());
            assertNotNull(page.getByXPath("//div[@class='gwt-Label']").get(0));
            HtmlInput htmlInput = (HtmlInput) page.getByXPath("//input[@id = 'searchTerm']").get(0);
            assertEquals("",htmlInput.getValueAttribute());
            htmlInput.setValueAttribute("pelv");
            assertEquals("pelv",htmlInput.getValueAttribute());
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testAnatomyLookupTyping(){
        try {
            login();
            webClient.waitForBackgroundJavaScriptStartingBefore(2000) ;
            HtmlPage page = webClient.getPage(ZfinProperties.NON_SECURE_HTTP+domain +"/action/dev-tools/gwt/lookup-table");
            webClient.waitForBackgroundJavaScriptStartingBefore(2000) ;
            assertEquals("GWT Lookup Table",page.getTitleText());
            final HtmlForm form = page.getFormByName("lookupTable");
            final HtmlTextInput textField = form.getInputByName("searchTerm");

            assertEquals("Lookup Table Application", ( (HtmlHeading1) page.getByXPath("//h1").get(0) ).getTextContent());
            assertEquals("",textField.getValueAttribute());

            textField.type("pelv");
            webClient.waitForBackgroundJavaScript(2000) ;

            assertEquals("pelv",textField.getValueAttribute());

            webClient.waitForBackgroundJavaScriptStartingBefore(2000) ;

            assertNotNull(page.getByXPath("//div[@class='gwt-SuggestBoxPopup']"));
            assertEquals(20,page.getByXPath("//span[@class='autocomplete-plain']").size() );
            assertTrue( ((HtmlSpan) page.getByXPath("//span[@class='autocomplete-plain']").get(19)).getTextContent().contains("pelv") );

            // the very first element is selected by default
            assertEquals(1,page.getByXPath("//td[@class='item item-selected']").size() );
            assertEquals(19,page.getByXPath("//td[@class='item']").size() );
            assertNotSame("...",((HtmlTableDataCell) page.getByXPath("//td[@class='item']").get(18)).getTextContent());

        } catch (Exception e) {
            fail(e.toString());
        }

    }

}
