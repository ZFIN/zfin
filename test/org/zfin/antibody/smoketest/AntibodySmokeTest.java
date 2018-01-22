package org.zfin.antibody.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class AntibodySmokeTest extends AbstractSmokeTest {

    public AntibodySmokeTest(WebClient webClient) {
        super(webClient);
    }


    /**
     * Just pull up the antibody search and check for no errors.
     */
    @Test
    public void testAntibodySearchPageOk() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/search");
        assertEquals("ZFIN Antibody search", "ZFIN Antibody Search", page.getTitleText());
    }

    /**
     * search for 'zn1':
     * result page displays zn-1 and zn-13 among other antibodies.
     */
    @Test
    public void testSearchZn1() throws IOException {
        String uri = "/action/antibody/antibody-do-search?antibodyCriteria.antibodyNameFilterType=contains&antibodyCriteria.name=zn1&maxDisplayRecords=25";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + uri);
        List<?> relatedTerms = page.getByXPath("//a[@id='zn-1']");
        assertEquals(1, relatedTerms.size());
        relatedTerms = page.getByXPath("//a[@id='zn-13']");
        assertEquals(1, relatedTerms.size());
    }

    @Test
    public void testAntibodyDetailPage() throws Exception {
        // name
        String zdbID = "ZDB-ATB-081002-3";
        String uri = "/" + zdbID;
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + uri);
        HtmlTableDataCell cell = (HtmlTableDataCell) page.getByXPath("//table[@class='data_manager']//td").get(0);
        assertTrue(cell.getTextContent().contains(zdbID));
        assertNotNull("find the ID on the page", page.getByXPath("//table[@class='data_manager']//td").get(0));
        assertNotNull("renders the end of the page sources ", page.getByXPath("//a[@href='http://zebrafish.org/']").get(0));
        assertNotNull("renders the end of the page citations ", page.getByXPath("//a[. ='CITATIONS']").get(0));
    }


    /**
     * search for 'ab-2':
     * result page displays AB-2F11 and other antibodies capitalized.
     */
    @Test
    public void testSearchAb_2() throws IOException {
        String uri = "/action/antibody/antibody-do-search?antibodyCriteria.antibodyNameFilterType=contains&antibodyCriteria.name=ab-2&maxDisplayRecords=25";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + uri);
        List<?> relatedTerms = page.getByXPath("//a[@id='ab-2f11']");
        assertEquals(1, relatedTerms.size());
        HtmlAnchor link = (HtmlAnchor) relatedTerms.get(0);
        assertEquals("Upper case indicates usage of the antibody name", "Ab-2F11", link.getTextContent());
    }

    /**
     * search for 'ab-2':
     * result page displays AB-2F11 and other antibodies capitalized.
     */
    public void searchCellularComponent() throws IOException {
        // nucleus
        String termID = "ZDB-TERM-091209-4086";
        String uri = "/action/antibody/antibody-do-search?antibodyCriteria.includeSubstructures=true&antibodyCriteria.anatomyTermNames=nuclear%20body&antibodyCriteria.anatomyTermIDs=" + termID + "&action=SEARCH";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + uri);
        List<?> relatedTerms = page.getByXPath("//a[@id='Ab-Pax7']");
        assertEquals(1, relatedTerms.size());
        HtmlAnchor link = (HtmlAnchor) relatedTerms.get(0);
        assertEquals("Upper case indicates usage of the antibody name", "Ab-2F11", link.getTextContent());
    }

    /**
     * search for 'zn5':
     * Check that the result page is the detail page of zn-5 as it is the only antibody with this name.
     * See FB case
     */
    @Test
    public void testSearchZn5() throws IOException {
        String uri = "/action/antibody/antibody-do-search?antibodyCriteria.antibodyNameFilterType=contains&antibodyCriteria.name=zn5&maxDisplayRecords=25";
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + uri);
        assertThat("Should have page title for detail page",
                page.getTitleText(), is("ZFIN Antibody: zn-5"));

        // Fashena et al. publication: check that this reference is used
        HtmlAnchor hyperlink = (HtmlAnchor) page.getElementById("ZDB-PUB-990507-16");
        assertThat("Link to Fashena et al. should be present",
                hyperlink.getHrefAttribute(), containsString("ZDB-PUB-990507-16"));

        // alcama antigen gene: check this antigen gene is present
        HtmlAnchor geneHyperlink = (HtmlAnchor) page.getElementById("ZDB-GENE-990415-30");
        HtmlPage genePage = geneHyperlink.click();
        // cannot check the title of the page as it is not in the <head> segment!
        //assertEquals("ZFIN: Gene: alcama", genePage.getTitleText());
        assertThat("Link to antigen gene page should load", genePage, notNullValue());

        // check figure view:  Fig. 7 from Chen et al., 2008
        hyperlink = (HtmlAnchor) page.getElementById("ZDB-FIG-140206-11");
        HtmlPage figurePage = hyperlink.click();
        assertThat("Link to figure page should load", figurePage, notNullValue());

        // Source: check ZIRC is one of them
        HtmlAnchor sourceHyperlink = (HtmlAnchor) page.getElementById("ZDB-LAB-991005-53");
        HtmlPage labPage = sourceHyperlink.click();
        assertThat("ZIRC link should load correctly",
                labPage.getTitleText(), is("Zebrafish International Resource Center"));

        // check alias
        HtmlSpan span = (HtmlSpan) page.getElementById("previous-name-0");
        assertThat("Synonym should be present", span.getTextContent(), startsWith("zn5 ("));

        // check host organism
        span = (HtmlSpan) page.getElementById("host-organism");
        assertThat("Host organism should be listed", span.getTextContent(), is("Mouse"));

        // check immunogen organism
        span = (HtmlSpan) page.getElementById("immunogen-organism");
        assertThat("Immunogen organism should be listed", span.getTextContent(), is("Zebrafish"));

        // check clonal type
        span = (HtmlSpan) page.getElementById("clonal-type");
        assertThat("Clonal type should be listed", span.getTextContent(), is("monoclonal"));
    }

    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermAllFigures() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=false");
        assertEquals("Antibody figure summary page is not coming up", "ZFIN Antibody figure summary: Ab1-eng", page.getTitleText());
        // check that Pub Zhou et al is present.
        assertNotNull(page.getElementById("ZDB-PUB-090407-2"));
    }

    /**
     * Check that antibody figure summary page comes up without stage and figWithImage Info.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermAllFiguresNoStageInfo() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10");
        assertEquals("Antibody figure summary page is not coming up", "ZFIN Antibody figure summary: Ab1-eng", page.getTitleText());
        // check that Pub Zhou et al is present.
        assertNotNull(page.getElementById("ZDB-PUB-090407-2"));
    }

    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermOnlyFiguresWithImages() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=true");
        assertEquals("Antibody figure summary page is not coming up", "ZFIN Antibody figure summary: Ab1-eng", page.getTitleText());
    }

    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermSubtermFiguresWithImages() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=ZDB-TERM-091209-4086&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=false");
        assertEquals("Antibody search", "ZFIN Antibody figure summary: Ab1-eng", page.getTitleText());
        // check that pub Liu et al is present.
        assertNotNull(page.getElementById("ZDB-PUB-091005-5"));
    }

    /**
     * Check that the link from the publication page to the list of antibodies is not broken.0
     */
    @Test
    public void testListOfAntibodiesLinkOnPublication() throws IOException {
        // Depletion of Zebrafish Essential and Regulator publication with at last 3 antibodies
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/ZDB-PUB-080326-24");
        assertTrue(page.getTitleText().contains("Chen et al"));

        List<HtmlAnchor> antibodiesLink = page.getAnchors();

        HtmlAnchor antibodyLinkAnchor = null;
        for (HtmlAnchor anchor : antibodiesLink)
            if (anchor.getHrefAttribute().contains("antibodies-per-publication"))
                antibodyLinkAnchor = anchor;
        HtmlPage antibodyListPage = webClient.getPage(nonSecureUrlDomain + antibodyLinkAnchor.getHrefAttribute());

        assertNotNull(antibodyListPage);
        assertTrue(antibodyListPage.getTitleText().contains("Chen"));

        // AB-F59 is one of a few antibodies that are used in this publication.
        HtmlAnchor antibodyLink = (HtmlAnchor) antibodyListPage.getElementById("ab-f59");
        assertNotNull(antibodyLink);

        // check that the link to the antibody view page is working as well.
        HtmlPage antibodyPage = webClient.getPage(nonSecureUrlDomain + antibodyLink.getHrefAttribute());

        assertNotNull(antibodyPage);
        assertTrue(antibodyPage.getTitleText().contains("ZFIN Antibody: Ab-F59"));
    }

}
