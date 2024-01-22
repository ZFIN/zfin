package org.zfin.sequence.blast.smoketest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zfin.AbstractSmokeTest;
import org.zfin.infrastructure.ActiveData;
import org.zfin.marker.Marker;
import org.zfin.mutant.SequenceTargetingReagent;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

@Log4j2
@RunWith(Parameterized.class)
public class BlastSmokeTest extends AbstractSmokeTest {

    public BlastSmokeTest(WebClient webClient) {
        super(webClient);
    }

    /**
     * Check if the blast page is accessible
     */
    @Test
    public void testBlastPageOk() {
        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/blast/blast");
                assertEquals("ZFIN: BLAST Query", "ZFIN: BLAST Query", page.getTitleText());

            } catch (IOException e) {
                fail(e.toString());
            }
        }
    }

    /**
     * Get a recent crispr STR from the database. Query the blast form with the sequence of the STR.
     * Make sure it returns a results page that contains the STR in the results.
     */
    @org.junit.Ignore //This fails on TRUNK -> TODO: write a jenkins job to replace this test
    @Test
    public void testRecentSTR() {
        SequenceTargetingReagent str = getRecentCrisprSTR();
        log.info("STR: " + str.getZdbID());
        log.info("Sequence: " + str.getSequence().getSequence());

        for (WebClient webClient : publicWebClients) {
            try {
                HtmlPage page = webClient.getPage(nonSecureUrlDomain + "/action/blast/blast");
                assertEquals("ZFIN: BLAST Query", "ZFIN: BLAST Query", page.getTitleText());

                //select zfin_crispr from the dropdown named dataLibraryString
                HtmlSelect select = (HtmlSelect) page.getElementByName("dataLibraryString");
                HtmlOption option = select.getOptionByValue("zfin_crispr");
                select.setSelectedAttribute(option, true);

                //fill in the textarea for sequences
                HtmlTextArea textArea = (HtmlTextArea) page.getElementById("querySequence");
                textArea.setText(str.getSequence().getSequence());

                //submit the form
                HtmlButtonInput button = (HtmlButtonInput) page.getFirstByXPath("//input[@type='button' and @value='Begin Search']");
                HtmlPage resultPage = button.click();

                String pageTitle = resultPage.getTitleText();
                String linkUrl = "";
                int maxTries = 10;
                int tries = 0;
                while(!pageTitle.startsWith("ZFIN: BLAST Results")) {
                    if (tries++ > maxTries) {
                        fail("Exceeded max tries of " + maxTries + " to get to results page");
                    }
                    Thread.sleep(3000);

                    // Click on the first link on the page main section (should be the link to the results)
                    HtmlAnchor resultLink = (HtmlAnchor) (resultPage.getElementsByTagName("main").get(0).getElementsByTagName("a").get(0));
                    String linkTitle = resultLink.getTextContent();

                    //link label should be just a long number
                    assertTrue("Expected link label to be a long number, but was: " + linkTitle, linkTitle.matches("^\\d+$"));

                    linkUrl = resultLink.getAttribute("href");

                    //link url should look like /action/blast/blast-view?resultFile=1142763059800883152, for example
                    assertTrue("Expected link URL to lead to results page, but was: " + linkUrl, linkUrl.contains("/action/blast/blast-view?resultFile=" + linkTitle));

                    // Click on the link to go to the results page if ready, otherwise wait and try again
                    resultPage = resultLink.click();
                    pageTitle = resultPage.getTitleText();
                }

                HtmlPage finalPage = resultPage;

                assertTrue("Expected title to be 'ZFIN: BLAST Results...', but was: " + finalPage.getTitleText(), finalPage.getTitleText().startsWith("ZFIN: BLAST Results"));
                assertTrue("Expected url to match the link url: " + finalPage.getUrl(), finalPage.getUrl().toString().contains(linkUrl));

                // Now on the redirected page, check for the presence of ID of the STR
                boolean isIdPresent = finalPage.getBody().getTextContent().contains(str.getZdbID());
                assertTrue("Expected ID '" + str.getZdbID() + "' not found on the results page.", isIdPresent);

            } catch (IOException e) {
                fail(e.toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //get a recent STR, but older than 48 hours to give it time to be entered into the blast database through jenkins
    private SequenceTargetingReagent getRecentCrisprSTR() {
        List<SequenceTargetingReagent> strs = getMarkerRepository().getRecentSequenceTargetingReagents(1000);

        for(SequenceTargetingReagent str : strs) {
            if(!str.getType().equals(Marker.Type.CRISPR)) {
                continue;
            }

            GregorianCalendar date = ActiveData.getDateFromId(str.getZdbID());

            //timestamp 48 hours before now
            GregorianCalendar cutoff = new GregorianCalendar();
            cutoff.add(GregorianCalendar.HOUR, -48);

            if(date.before(cutoff)) {
                return str;
            }
        }
        throw new RuntimeException("No CRISPR STRs found older than 48 hours");
    }

}
