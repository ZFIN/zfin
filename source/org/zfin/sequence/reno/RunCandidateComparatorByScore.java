package org.zfin.sequence.reno;

import java.util.Comparator;


public class RunCandidateComparatorByScore implements Comparator {
    public int compare(Object rc1, Object rc2) {
        Integer s1 = ((RunCandidate)rc1).getBestHit().getScore();
        Integer s2 = ((RunCandidate)rc2).getBestHit().getScore();
        return s2.compareTo(s1);
    }
}