package org.zfin.marker;

import java.util.Comparator;

public class MarkerTypeSignificanceComparator<T extends MarkerType> implements Comparator<T> {

    public int compare(T o1, T o2) {
        if (o1 == null && o2 == null)
            return -1;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return +1;

        return o1.getSignificance().compareTo(o2.getSignificance());
    }

}
