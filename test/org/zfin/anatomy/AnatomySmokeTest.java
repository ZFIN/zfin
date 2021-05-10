package org.zfin.anatomy;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class AnatomySmokeTest extends AbstractSmokeTest {

    public AnatomySmokeTest(WebClient webClient) {
        super(webClient);
    }

    @Test
    public void testAnatomyLookupFormExists() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/search");
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        assertEquals("ZFIN AO / GO Search", page.getTitleText());
        List<?> byXPath = page.getByXPath("//label[. = 'Term:']");
        assertNotNull(byXPath);
        assertNotNull(byXPath.get(0));
        List<?> byXPath1 = page.getByXPath("//input[@id = 'searchTerm']");
        assertNotNull(byXPath1);
        HtmlInput htmlInput = (HtmlInput) byXPath1.get(0);
        assertNotNull(htmlInput);
        assertEquals("", htmlInput.getValueAttribute());
        htmlInput.setValueAttribute("pelv");
        assertEquals("pelv", htmlInput.getValueAttribute());
        List<FrameWindow> iFrames = page.getFrames();
        assertNotNull(iFrames);
    }

    @Test
    public void testAnatomySearchMultipleResults() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/term?name=emb*");
        assertEquals("ZFIN Ontology Search", page.getTitleText());
        // check that embryonic structure is listed
        List<HtmlSpan> caption = (List<HtmlSpan>) page.getByXPath("//a[@name = 'embryonic structure']");
        assertNotNull(caption);
    }

    @Test
    public void testAllAnatomyTerms() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-anatomy-terms");
        assertEquals("ZFIN", page.getTitleText());
        // check that embryonic structure is listed
        List<HtmlSpan> caption = (List<HtmlSpan>) page.getByXPath("//a[@name = 'embryonic structure']");
        assertNotNull(caption);
    }


    @Test
    public void testAnatomyDetailPageByAnatId() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/ZDB-ANAT-010921-415");
        assertEquals("ZFIN Anatomy Ontology: brain", page.getTitleText());
        List<?> byXPath = page.getByXPath("//a[. = 'PHENOTYPE']");
        assertNotNull("Phenotype section is not displayed", byXPath);
        assertTrue("Phenotype section is not displayed", byXPath.size() > 0);
    }

    // liver page
    @Test
    public void testAnatomyDetailPageByTermId() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/ZDB-TERM-100331-116");
        assertEquals("ZFIN Anatomy Ontology: liver", page.getTitleText());
        List<?> byXPath = page.getByXPath("//a[. = 'PHENOTYPE']");
        assertNotNull("Phenotype section is not displayed", byXPath);
        assertTrue("Phenotype section is not displayed", byXPath.size() > 0);
        assertNotNull(byXPath.get(0));
    }

    // liver page
    @Test
    public void testAnatomyDetailPageByOboId() throws IOException {
        webClient.waitForBackgroundJavaScriptStartingBefore(1);
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/ZFA:0000123");
        assertEquals("ZFIN Anatomy Ontology: liver", page.getTitleText());
        assertNotNull(page.getByXPath("//a[. = 'PHENOTYPE']").get(0));
    }

    // liver page
    @Test
    public void testAnatomyDetailPageByName() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/term?name=liver ");
        assertNotNull(page);
        assertEquals("ZFIN Anatomy Ontology: liver", page.getTitleText());
        assertNotNull(page.getByXPath("//a[. = 'PHENOTYPE']").get(0));
    }

    @Test
    // brain
    public void testShowAllMutantMorpholinos() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-clean-fish/ZDB-TERM-100331-8");
        assertTrue(page.getTitleText().startsWith("ZFIN"));
        assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENE-070521-2']").get(0));
    }

    //floor plate
    @Test
    public void testShowAllMutants() throws IOException {
        // vhl^hu2081/+;vhl^hu2117/+;Tg(kdrl:EGFP)s843
        String genoID = "ZDB-GENO-100524-4";
        // mutants in retina
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-clean-fish/ZDB-TERM-100331-1770");
        assertTrue(page.getTitleText().startsWith("ZFIN"));
        assertThat(page.getAnchorByText("vhlhu2081/+; vhlhu2117/+; s843Tg"), is(notNullValue()));
    }

    /**
     * Check that the expressed gene section is loaded correctly.
     */
    @Test
    public void testAnatomyDetailPageExpressedGenes() throws IOException {
        // 	telencephalic ventricle [ZDB-TERM-100331-665]
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-expressed-genes/ZDB-TERM-100331-665");
        // gene creb1a with 3 figures
        assertNotNull(page.getByXPath("//a[@id='ZDB-GENE-040426-750']").get(0));
    }

    /**
     * Check that the antibodies section is loaded correctly.
     */
    @Test
    public void testAnatomyDetailPageAntibodies() throws IOException {
        webClient.waitForBackgroundJavaScriptStartingBefore(1);
        // 	brain [ZDB-TERM-100331-8]
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-labeled-antibodies/ZDB-TERM-100331-8");
        // antibody Ab1-5-hmC
        assertNotNull("Could not find antibody Ab1-5-hmc", page.getByXPath("//a[@id='ab1-5-hmc']").get(0));
    }

    /**
     * Check that the antibodies section is loaded correctly.
     */
    @Test
    public void testAnatomyDetailPageInSituProbe() throws IOException {
        webClient.waitForBackgroundJavaScriptStartingBefore(1);
        // 	brain [ZDB-TERM-100331-8]
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-expressed-insitu-probes/ZDB-TERM-100331-8");
        // gene crhbp with 3 figures
        assertNotNull(page.getByXPath("//a[@id='ZDB-GENE-040801-196']").get(0));
    }

    /**
     * Check that a list of genotypes for a given structure is rendered.
     */
    @Test
    public void testShowGenotypesPerAOSubstructures() throws IOException {
        webClient.waitForBackgroundJavaScriptStartingBefore(1000);
        // 	actinotrichium [ZDB-TERM-100614-30]
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-clean-fish-include-substructures/ZDB-TERM-100614-30");
        // Genotype Df(Chr03:sox8,sox9b)b971/b971
        assertThat(page.getAnchorByText("AB + MO1-plod1a"), is(notNullValue()));
    }


}
