package org.zfin.sequence.reno.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RedundancyRun;

public class RunPresentationTest {

    private Run run;

    @Before
    public void setUp() {
        TestConfiguration.initApplicationProperties();
        run = new RedundancyRun() ;
        run.setZdbID("ZDB-RUN-090227278-0");
        run.setName("Test Run");
    }

    /**
     * Create a marker hyperlink with the zdbID in the URL and
     * a marker specific span-tag including style sheet.
     */
    @Test
    public void markerLink() {
        String link = RunPresentation.getLink(run);
        assertEquals("Hyperlink", "<a href=\"/action/reno/candidate-inqueue?zdbID=ZDB-RUN-090227278-0\" name=\"\">Test Run</a>", link);
    }

}
