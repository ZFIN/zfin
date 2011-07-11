package org.zfin.sequence.reno.presentation;

import org.junit.Before;
import org.junit.Test;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.Run;

import static org.junit.Assert.assertEquals;

public class RunPresentationTest {

    private Run run;

    @Before
    public void setUp() {
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
        assertEquals("Hyperlink", "<a href=\"/action/reno/candidate-inqueue?zdbID=ZDB-RUN-090227278-0\" id='ZDB-RUN-090227278-0'>Test Run</a>", link);
    }

}
