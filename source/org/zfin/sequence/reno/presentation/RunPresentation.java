package org.zfin.sequence.reno.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.sequence.reno.Run;

/**
 * Presentation Class to create output from a Run object.
 */
public class RunPresentation extends EntityPresentation {

    private static final String uri = "reno/candidate-inqueue?zdbID=";

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @return html for marker link
     * @param run Run
     */
    public static String getLink(Run run) {
        return getTomcatLink(uri, run.getZdbID(), run.getName());
    }

}
