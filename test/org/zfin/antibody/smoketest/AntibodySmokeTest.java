package org.zfin.antibody.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import org.junit.Test;
import org.zfin.AbstractSmokeTest;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.io.IOException;
import java.util.List;

/**
 */
public class AntibodySmokeTest extends AbstractSmokeTest {

    /**
     * Just pull up the antibody search and check for no errors.
     */
    public void testAntibodySearchPageOk() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                HtmlPage page = webClient.getPage("http://" + domain + "/action/antibody/search");
                assertEquals("Antibody search", "Antibody Search", page.getTitleText());

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * search for 'zn1':
     * result page displays zn-1 and zn-13 among other antibodies.
     */
    public void testSearchZn1() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                String uri = "/action/antibody/search?antibodyCriteria.antibodyNameFilterType=contains&antibodyCriteria.name=zn1&maxDisplayRecords=25&action=SEARCH";
                HtmlPage page = webClient.getPage("http://" + domain + uri);
                List<?> relatedTerms = page.getByXPath("//a[@id='zn-1']");
                assertEquals(1, relatedTerms.size());
                relatedTerms = page.getByXPath("//a[@id='zn-13']");
                assertEquals(1, relatedTerms.size());

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    @Test
    public void testAntibodyDetailPage() throws Exception {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                // name
                String zdbID = "ZDB-ATB-081002-3" ;
                String uri = "/action/antibody/detail?antibody.zdbID="+zdbID;
                HtmlPage page = webClient.getPage(ZfinPropertiesEnum.NON_SECURE_HTTP + domain + uri);
                HtmlTableDataCell cell = (HtmlTableDataCell) page.getByXPath("//table[@class='data_manager']//td").get(0);
                assertTrue(cell.getTextContent().contains(zdbID)) ;
                assertNotNull("find the ID on the page" , page.getByXPath("//table[@class='data_manager']//td").get(0));
                assertNotNull("renders the end of the page sources " , page.getByXPath("//a[@id='ZDB-LAB-991005-53']").get(0));
                assertNotNull("renders the end of the page citations " , page.getByXPath("//a[. ='CITATIONS']").get(0));

            }
            catch(Exception e){
                fail(e.toString()) ;
            }
        }
    }


    /**
     * search for 'ab-2':
     * result page displays AB-2F11 and other antibodies capitalized.
     */
    public void testSearchAb_2() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                String uri = "/action/antibody/search?antibodyCriteria.antibodyNameFilterType=contains&antibodyCriteria.name=ab-2&maxDisplayRecords=25&action=SEARCH";
                HtmlPage page = webClient.getPage("http://" + domain + uri);
                List<?> relatedTerms = page.getByXPath("//a[@id='ab-2f11']");
                assertEquals(1, relatedTerms.size());
                HtmlAnchor link = (HtmlAnchor) relatedTerms.get(0);
                assertEquals("Upper case indicates usage of the antibody name", "Ab-2F11", link.getTextContent());
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * search for 'zn5':
     * Check that the result page is the detail page of zn-5 as it is the only antibody with this name.
     * See FB case
     */
    public void testSearchZn5() {
        for (WebClient aWebClient : publicWebClients) {
            webClient = aWebClient;
            try {
                String uri = "/action/antibody/search?antibodyCriteria.antibodyNameFilterType=contains&antibodyCriteria.name=zn5&maxDisplayRecords=25&action=SEARCH";
                HtmlPage page = webClient.getPage("http://" + domain + uri);
                assertEquals("Antibody detail page", "Antibody: zn-5", page.getTitleText());
                // Fashena et al. publication: check that this reference is used
                HtmlAnchor hyperlink = (HtmlAnchor) page.getElementById("ZDB-PUB-990507-16");
                HtmlPage publicationPage = hyperlink.click();
                assertEquals("ZFIN: Publication: Fashena et al., 1999. Secondary motoneuron axons localize DM-GRASP on their fasciculated segments", publicationPage.getTitleText());

                // alcama antigen gene: check this antigen gene is present
                HtmlAnchor geneHyperlink = (HtmlAnchor) page.getElementById("ZDB-GENE-990415-30");
                HtmlPage genePage = geneHyperlink.click();
                // cannot check the title of the page as it is not in the <head> segment!
                //assertEquals("ZFIN: Gene: alcama", genePage.getTitleText());
                assertNotNull(genePage);

                // check figure view:  Ke et al., 2008
                hyperlink = (HtmlAnchor) page.getElementById("ZDB-FIG-080325-45");
                HtmlPage figurePage = hyperlink.click();
                assertNotNull(figurePage);


                // Source: check ZIRC is one of them
                HtmlAnchor sourceHyperlink = (HtmlAnchor) page.getElementById("ZDB-LAB-991005-53");
                assertEquals("Zebrafish International Resource Center (ZIRC)", sourceHyperlink.getTextContent());

                // check source link to lab detail
                HtmlPage labPage = sourceHyperlink.click();
                assertEquals("Zebrafish International Resource Center", labPage.getTitleText());

                // check alias
                HtmlSpan span = (HtmlSpan) page.getElementById("zn5");
                assertEquals("zn5", span.getTextContent());

                // check host organism
                span = (HtmlSpan) page.getElementById("host organism");
                assertEquals("Mouse", span.getTextContent());

                // check immunogen organism
                span = (HtmlSpan) page.getElementById("immunogen organism");
                assertEquals("Zebrafish", span.getTextContent());

                // check clonal type
                span = (HtmlSpan) page.getElementById("clonal type");
                assertEquals("monoclonal", span.getTextContent());
            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

}