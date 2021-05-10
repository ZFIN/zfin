package org.zfin.sequence.reno;

import java.util.Comparator;

public class RunCandidateComparatorByName implements Comparator {

    public int compare(Object rc1, Object rc2) {
        String name1 = ((RunCandidate) rc1).getCandidate().getName();
        String name2 = ((RunCandidate) rc2).getCandidate().getName();
        return name1.compareToIgnoreCase(name2);
    }
}
