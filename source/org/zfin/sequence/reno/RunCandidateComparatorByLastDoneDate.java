package org.zfin.sequence.reno;

import java.util.Comparator;
import java.util.Date;

public class RunCandidateComparatorByLastDoneDate implements Comparator {
    public int compare(Object rc1, Object rc2) {
        Date d1 = ((RunCandidate)rc1).getCandidate().getLastFinishedDate();
        Date d2 = ((RunCandidate)rc2).getCandidate().getLastFinishedDate();
        return d1.compareTo(d2);

    }

}

