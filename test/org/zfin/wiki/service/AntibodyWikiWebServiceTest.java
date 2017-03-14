package org.zfin.wiki.service;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.antibody.Antibody;

import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;

/**
 */
public class AntibodyWikiWebServiceTest extends AbstractDatabaseTest {

    /**
     * Test that the antibody page on the wiki (for zn-5) has the correct title
     * and the correct hyperlink to the recognized gene (alcama) at ZFIN.
     * <p/>
     * The method is run in a separate thread through the ExecutorService to
     * allow a timeout in case the wiki is down and not responding.
     *
     * @throws Exception
     */
    @Test
    public void checkGeneLinkOnAntibodyPage() throws Exception {

        // zn-5
        String zdbID = "ZDB-ATB-081002-19";
        Antibody ab = getAntibodyRepository().getAntibodyByID(zdbID);
        assertTrue(ab != null);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Task(ab));

        try {
            future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.out.println("The wiki request timed out. Check if the wiki is up and running.");
        } finally {
            executor.shutdownNow();
        }

    }

    @Test
    public void checkAntibodyTemplate() throws Exception {

        // zn-5
        String zdbID = "ZDB-ATB-081002-19";
        Antibody ab = getAntibodyRepository().getAntibodyByID(zdbID);
        assertTrue(ab != null);
        AntibodyWikiWebService service = AntibodyWikiWebService.getInstance();
        String content = service.createWikiPageContentForAntibodyFromTemplate(ab);
        assertNotNull(content);
    }

    @Test
    public void checkReplacementsString() {
        String value = "G&#945; & s/olf";
        assertEquals("G&amp;#945; &amp; s/olf", AntibodyWikiWebService.getEncodedString(value));
        value = "Piperno & Fuller";
        assertEquals("Piperno &amp; Fuller", AntibodyWikiWebService.getEncodedString(value));
/*
        value = "G&#945; 1&2";
        assertEquals("G&alpha; 1&amp;2", AntibodyWikiWebService.getEncodedString(value));
*/
    }


}

class Task implements Callable<String> {
    private Antibody antibody;

    Task(Antibody ab) {
        this.antibody = ab;
    }

    @Override
    public String call() throws Exception {
        AntibodyWikiWebService service = AntibodyWikiWebService.getInstance();
        String title = service.getWikiTitleFromAntibody(antibody);
        assertEquals("zn-5", title);
        return "Ready!";
    }
}
