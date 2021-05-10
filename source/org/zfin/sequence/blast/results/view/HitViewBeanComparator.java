package org.zfin.sequence.blast.results.view;

import java.util.Comparator;

/**
 * Used for sorting HitViewBeans by their hit ID
 */
public class HitViewBeanComparator implements Comparator<HitViewBean> {

    public HitViewBeanComparator(){}

    public int compare(HitViewBean o1, HitViewBean o2) {
        return o1.getHitNumber()- o2.getHitNumber() ;
    }
}
