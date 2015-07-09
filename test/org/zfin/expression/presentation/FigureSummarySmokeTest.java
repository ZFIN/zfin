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
        assertEquals("Antibody search", "ZFIN Antibody figure summary: Ab1-eng", page.getTitleText());
        // check that Zhou et al. paper is present.
        assertNotNull(page.getElementById("ZDB-PUB-090407-2"));
    }

    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermOnlyFiguresWithImages() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=true");
        assertEquals("Antibody search", "ZFIN Antibody figure summary: Ab1-eng", page.getTitleText());
    }

    /**
     * Check that antibody figure summary page comes up.
     */
    @Test
    public void testAntibodyFigureSummaryPageSupertermSubtermFiguresWithImages() throws IOException {
        HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/antibody/antibody-figure-summary?antibodyID=ZDB-ATB-081017-1&superTermID=ZDB-TERM-100331-1053&subTermID=ZDB-TERM-091209-4086&startStageID=ZDB-STAGE-010723-10&endStageID=ZDB-STAGE-010723-10&figuresWithImg=false");
        assertEquals("Antibody search", "ZFIN Antibody figure summary: Ab1-eng", page.getTitleText());
        // check that publication Liu et al is present.
        assertNotNull(page.getElementById("ZDB-PUB-091005-5"));
    }
}

