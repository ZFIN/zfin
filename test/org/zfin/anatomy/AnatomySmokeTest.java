package org.zfin.anatomy;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.zfin.AbstractSmokeTest;

import java.util.List;

/**
 */
public class AnatomySmokeTest extends AbstractSmokeTest {

    public void testAnatomyLookupFormExists() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/anatomy-search");
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
                assertEquals(1, iFrames.size());
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    public void testAnatomySearchMultipleResults() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/term?name=emb*");
                assertEquals("ZFIN", page.getTitleText());
                // check that embryonic structure is listed
                List<HtmlSpan> caption = (List<HtmlSpan>) page.getByXPath("//a[@name = 'embryonic structure']");
                assertNotNull(caption);
                assertEquals(1, caption.size());
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    public void testAllAnatomyTerms() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-anatomy-terms");
                assertEquals("ZFIN", page.getTitleText());
                // check that embryonic structure is listed
                List<HtmlSpan> caption = (List<HtmlSpan>) page.getByXPath("//a[@name = 'embryonic structure']");
                assertNotNull(caption);
                assertEquals(1, caption.size());
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


    public void testAnatomyDetailPageByAnatId() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/ZDB-ANAT-010921-415");
                assertEquals("ZFIN Anatomy Ontology: brain", page.getTitleText());
                assertNotNull(page.getByXPath("//a[. = 'about']").get(0));
                List<?> byXPath = page.getByXPath("//a[. = 'PHENOTYPE']");
                assertNotNull("Phenotype section is not displayed", byXPath);
                assertTrue("Phenotype section is not displayed", byXPath.size() > 0);
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // liver page
    public void testAnatomyDetailPageByTermId() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/ZDB-TERM-100331-116");
                assertEquals("ZFIN Anatomy Ontology: liver", page.getTitleText());
                assertNotNull(page.getByXPath("//a[. = 'about']").get(0));
                List<?> byXPath = page.getByXPath("//a[. = 'PHENOTYPE']");
                assertNotNull("Phenotype section is not displayed", byXPath);
                assertTrue("Phenotype section is not displayed", byXPath.size() > 0);
                assertNotNull(byXPath.get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // liver page
    public void testAnatomyDetailPageByOboId() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/ZFA:0000123");
                assertEquals("ZFIN Anatomy Ontology: liver", page.getTitleText());
                assertNotNull(page.getByXPath("//a[. = 'about']").get(0));
                assertNotNull(page.getByXPath("//a[. = 'PHENOTYPE']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // liver page
    public void testAnatomyDetailPageByName() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/term-detail/term?name=liver ");
                assertNotNull(page);
                assertEquals("ZFIN Anatomy Ontology: liver", page.getTitleText());
                assertNotNull(page.getByXPath("//a[. = 'about']").get(0));
                assertNotNull(page.getByXPath("//a[. = 'PHENOTYPE']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // brain
    public void testShowAllMutantMorpholinos() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-morpholinos/ZDB-TERM-100331-8/false");
                assertTrue(page.getTitleText().startsWith("ZFIN"));
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENE-980526-362']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    // neural tube
    public void testShowAllWTMorpholinos() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-morpholinos/ZDB-TERM-100331-1095/true");
                assertTrue(page.getTitleText().startsWith("ZFIN"));
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENE-030826-1']"));
                assertTrue(page.getByXPath("//a[@id = 'ZDB-GENE-030826-1']").size() > 0);
                assertNotNull(page.getByXPath("//a[@id = 'ZDB-GENE-030826-1']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    //floor plate
    public void testShowAllMutants() {
        String genoID = "ZDB-GENO-050110-1";
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-phenotype-mutants/ZDB-TERM-100331-21");
                assertTrue(page.getTitleText().startsWith("ZFIN"));
                assertNotNull(page.getByXPath("//a[@id = '" + genoID + "']"));
                assertTrue(page.getByXPath("//a[@id = '" + genoID + "']").size() > 0);
                assertNotNull(page.getByXPath("//a[@id = '" + genoID + "']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Check that the expressed gene section is loaded correctly.
     */
    public void testAnatomyDetailPageExpressedGenes() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                // 	telencephalic ventricle [ZDB-TERM-100331-665]
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-expressed-genes/ZDB-TERM-100331-665");
                // gene creb1a with 3 figures
                assertNotNull(page.getByXPath("//a[@id='ZDB-GENE-040426-750']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Check that the antibodies section is loaded correctly.
     */
    public void testAnatomyDetailPageAntibodies() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                // 	brain [ZDB-TERM-100331-8]
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-labeled-antibodies/ZDB-TERM-100331-8");
                // antibody Ab1-5-hmC
                assertNotNull("Could not find antibody Ab1-5-hmc", page.getByXPath("//a[@id='ab1-5-hmc']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Check that the antibodies section is loaded correctly.
     */
    public void testAnatomyDetailPageInSituProbe() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(2000);
                // 	brain [ZDB-TERM-100331-8]
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-expressed-insitu-probes/ZDB-TERM-100331-8");
                // gene crhbp with 3 figures
                assertNotNull(page.getByXPath("//a[@id='ZDB-GENE-040801-196']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Check that a list of genotypes for a given structure is rendered.
     */
    public void testShowGenotypesPerAOSubstructures() {
        for (WebClient webClient : publicWebClients) {
            try {
                webClient.waitForBackgroundJavaScriptStartingBefore(1000);
                // 	actinotrichium [ZDB-TERM-100614-30]
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/ontology/show-all-phenotype-mutants-substructures/ZDB-TERM-100614-30");
                // Genotype Df(Chr03:sox8,sox9b)b971/b971
                assertNotNull(page.getByXPath("//a[@id='ZDB-GENO-050322-1']").get(0));
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }


}