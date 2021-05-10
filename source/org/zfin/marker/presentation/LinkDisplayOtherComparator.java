package org.zfin.marker.presentation;

import org.zfin.util.NumberAwareStringComparator;

import java.util.Comparator;

/**
 */
public class LinkDisplayOtherComparator implements Comparator<LinkDisplay>{

    private NumberAwareStringComparator numberAwareStringComparator = new NumberAwareStringComparator();

    @Override
    public int compare(LinkDisplay linkA, LinkDisplay linkB) {
        int compare;
        if (linkA.getSignificance() != null & linkB.getSignificance() != null) {
            compare = linkA.getSignificance().compareTo(linkB.getSignificance());
            if (compare != 0) return compare;
        }

        compare = linkA.getReferenceDatabaseName().compareTo(linkB.getReferenceDatabaseName());
        if (compare != 0) return compare;

        return numberAwareStringComparator.compare(linkA.getDisplayName(), linkB.getDisplayName());
    }
}
