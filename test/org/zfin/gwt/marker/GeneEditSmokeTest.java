package org.zfin.gwt.marker;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSecureSmokeTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

@RunWith(Parameterized.class)
public class GeneEditSmokeTest extends AbstractSecureSmokeTest {

    public GeneEditSmokeTest(WebClient webClient) throws Exception {
        super(webClient);
    }

    @Test
    public void testAlternateGeneNote() throws Exception {
        // first we have to guarantee that we always have a note there
        Marker gene = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-001103-2");
        String oldNote = gene.getPublicComments();
        if (StringUtils.isEmpty(oldNote)) {
            HibernateUtil.createTransaction();
            gene.setPublicComments("new note");
            HibernateUtil.flushAndCommitCurrentSession();
        }

//            final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
        HtmlPage page = webClient.getPage(secureUrlDomain + "/action/devtool/gwt/alternate-gene-edit");
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
    }

}
