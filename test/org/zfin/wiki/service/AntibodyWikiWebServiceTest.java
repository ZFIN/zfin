package org.zfin.wiki.service;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.antibody.Antibody;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        String contents = service.createWikiPageContentForAntibodyFromTemplate(antibody);
        assertTrue("contains alcama gene", contents.contains("alcama"));
        assertTrue("contains hyperlink to alcama gene", contents.contains("/action/marker/view/ZDB-GENE-990415-30"));
        return "Ready!";
    }
}

class TestTask implements Callable<String> {
    @Override
    public String call() throws Exception {
        Thread.sleep(4000); // Just to demo a long running task of 4 seconds.
        return "Ready!";
    }
}

