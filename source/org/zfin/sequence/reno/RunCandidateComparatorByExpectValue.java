package org.zfin.sequence.reno;

import java.util.Comparator;

public class RunCandidateComparatorByExpectValue implements Comparator{
    public int compare(Object rc1, Object rc2) {
        Double d1 = ((RunCandidate)rc1).getBestHit().getExpectValue();
        Double d2 = ((RunCandidate)rc2).getBestHit().getExpectValue();
        return d1.compareTo(d2);
    }
}

