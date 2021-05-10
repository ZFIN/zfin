package org.zfin.sequence.reno.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.sequence.reno.Run;

/**
 * Presentation Class to create output from a Run object.
 */
public class RunPresentation extends EntityPresentation {

    private static final String uri = "reno/candidate/"+ CandidateType.INQUEUE_CANDIDATES.toString()+"/";

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @return html for marker link
     * @param run Run
     */
    public static String getLink(Run run) {
        return getTomcatLink(uri, run.getZdbID(), run.getName(), null);
    }

    public static enum CandidateType {
        INQUEUE_CANDIDATES("inqueue"),
        PENDING_CANDIDATES("pending"),;

        private String value;

        private CandidateType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public static CandidateType getType(String type) {
            for (CandidateType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No CandidateType named " + type + " found.");
        }
    }
}
