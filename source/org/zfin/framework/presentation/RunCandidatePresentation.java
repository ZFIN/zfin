package org.zfin.framework.presentation;

import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.Marker;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.Candidate;
import org.apache.commons.lang.StringUtils;

/**
 * Presentation Class to create output from a Candidate object.
 */
public class RunCandidatePresentation extends EntityPresentation {

    private static final String PROBLEM = "&nbsp;<span title=\"is problem\">(p)</span>";

    public static String getLink(RunCandidate rc) {
        StringBuilder sb = new StringBuilder();
        if (rc.getCandidate().getSuggestedName() != null){
            sb.append(rc.getCandidate().getSuggestedName());
        }
        else {
            for (Marker m : rc.getIdentifiedMarkers()) {
                if (!StringUtils.isEmpty(sb.toString())) sb.append(", ");
                sb.append(MarkerPresentation.getLink(m));
            }
        }

        if (rc.getCandidate().isProblem())
            sb.append(PROBLEM);

        return sb.toString();
    }

    public static String getName(RunCandidate rc) {
        StringBuilder sb = new StringBuilder();

        Candidate candidate = rc.getCandidate();
        if (candidate.getSuggestedName() != null)
           sb.append(candidate.getSuggestedName());
        else {
            for (Marker m : rc.getIdentifiedMarkers()) {
                if (!StringUtils.isEmpty(sb.toString())) sb.append(", ");
                sb.append(MarkerPresentation.getAbbreviation(m));
            }
        }

        if (rc.getCandidate().isProblem()){
            sb.append(PROBLEM);
        }

        return sb.toString();

    }


}
