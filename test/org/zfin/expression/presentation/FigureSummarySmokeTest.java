package org.zfin.expression.presentation;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;

import java.io.IOException;

@RunWith(Parameterized.class)
public class FigureSummarySmokeTest extends AbstractSmokeTest {

    public FigureSummarySmokeTest(WebClient webClient) {
        super(webClient);
    }


    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermAllFigures() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=false");
        assertEquals("Antibody search", "ZFIN Antibody figure summary: Ab-eng", page.getTitleText());
        // check that Zhou et al. paper is present.
        assertNotNull(page.getElementById("ZDB-PUB-090407-2"));
    }

    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermOnlyFiguresWithImages() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=true");
        assertEquals("Antibody search", "ZFIN Antibody figure summary: Ab-eng", page.getTitleText());
    }

    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermSubtermFiguresWithImages() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=ZDB-TERM-091209-4086&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=false");
        assertEquals("Antibody search", "ZFIN Antibody figure summary: Ab-eng", page.getTitleText());
        // check that publication Liu et al is present.
        assertNotNull(page.getElementById("ZDB-PUB-091005-5"));
    }


    /**
     * Check specific genox genotype expression figure summary page
     */
    @Test
    public void testGenotypeExpressionFigureSummaryForGenox() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/expression/genotype-figure-summary?genoZdbID=ZDB-GENO-070912-1&expZdbID=ZDB-EXP-080331-13&geneZdbID=ZDB-GENE-000210-8&imagesOnly=false");
        // check that Thummel pub is present
        assertNotNull(page.getElementById("ZDB-PUB-080102-6"));
    }

    /**
     * Test isStandard (or GC) genotype expression figure summary page
     */
    @Test
    public void testGenotypeExpressionFigureSummaryForStandard() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/expression/genotype-figure-summary-standard?genoZdbID=ZDB-GENO-980202-822&geneZdbID=ZDB-GENE-990415-17&imagesOnly=false");
        // check that Jaszai pub is present
        assertNotNull(page.getElementById("ZDB-PUB-081031-1"));
    }

    /**
     * Test isChemcial  genotype expression figure summary page
     */
    @Test
    public void testGenotypeExpressionFigureSummaryForChemical() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/expression/genotype-figure-summary-chemical?genoZdbID=ZDB-GENO-070411-2&geneZdbID=ZDB-GENE-980526-283&imagesOnly=false");
        // check that Hernandez pub is present
        assertNotNull(page.getElementById("ZDB-PUB-061227-41"));
    }
}

