package org.zfin.ontology.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.zfin.AbstractSmokeTest;

import java.util.List;

/**
 */
public class OntologySmokeTest extends AbstractSmokeTest {

    public void testOntologyDetailPageByName() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                String termName = "mitochondrion";
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/term?name=" + termName + "&ontologyName=cellular_component");
                assertEquals("ZFIN GO: Cellular Component: " + termName, page.getTitleText());
                HtmlTableDataCell tdElement = (HtmlTableDataCell) page.getByXPath("//td[@id = 'ontology-name']").get(0);
                assertTrue(tdElement.getTextContent().startsWith("GO: Cellular Component"));
                List<HtmlAnchor> hyperlinks = (List<HtmlAnchor>) tdElement.getByXPath("a");
                // check that there are two links
                assertEquals(2, hyperlinks.size());
                assertEquals("QuickGO", hyperlinks.get(0).getTextContent());
                assertEquals("AmiGO", hyperlinks.get(1).getTextContent());
                tdElement = (HtmlTableDataCell) page.getByXPath("//td[@id = 'term-definition']").get(0);
                assertTrue(tdElement.getTextContent().startsWith("A semiautonomous"));
                // check relationships
                List<HtmlTableRow> relationshipRows = (List<HtmlTableRow>) page.getByXPath("//tr[@id = 'is-a-type-of']");
                // one is part of row
                assertEquals(1, relationshipRows.size());
                assertTrue( relationshipRows.get(0).getByXPath("td/div/span[@id = 'cytoplasmic part']").size() > 0);
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // test for aliases as well.
    public void testOntologyDetailPageByID() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                String termName = "developmental process";
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/GO:0032502");
                assertEquals("ZFIN GO: Biological Process: " + termName, page.getTitleText());
                HtmlTableDataCell tdElement = (HtmlTableDataCell) page.getByXPath("//td[@id = 'ontology-name']").get(0);
                assertTrue(tdElement.getTextContent().startsWith("GO: Biological Process"));
                // check the synonym
                assertNotNull(page.getByXPath("//td[@id = 'term-synonyms']"));
                tdElement = (HtmlTableDataCell) page.getByXPath("//td[@id = 'term-synonyms']").get(0);
                // one synonym
                assertEquals(1, tdElement.getByXPath("span").size());
                assertEquals("development", ((HtmlSpan)tdElement.getByXPath("span").get(0)).getTextContent());

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // test post-composed statement page
    public void testPostComposedStatementPage() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                String pageTitle = "ZFIN Post-Composed Term: intestine cytoplasm";
                // intestine : cytoplasm
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/post-composed-term-detail?superTermID=ZFA:0001338&subTermID=GO:0005737");
                // check title
                assertEquals(pageTitle, page.getTitleText());
                HtmlSpan span = (HtmlSpan) page.getByXPath("//span[@class = 'post-composed-term-name']").get(0);
                // check term name
                assertTrue(span.getTextContent().contains("intestine"));
                assertTrue(span.getTextContent().contains("cytoplasm"));

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // test post-composed statement page
    public void testPostComposedStatementPopupPage() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                // intestine : cytoplasm
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/post-composed-term-detail-popup?superTermID=ZFA:0001338&subTermID=GO:0005737");
//                // check title: there is no title
                assertEquals("", page.getTitleText());
                HtmlSpan span = (HtmlSpan) page.getByXPath("//span[@class = 'post-composed-term-name']").get(0);
                // check term name
                assertTrue(span.getTextContent().contains("intestine"));
                assertTrue(span.getTextContent().contains("cytoplasm"));

            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


}