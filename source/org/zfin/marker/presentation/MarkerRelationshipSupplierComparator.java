package org.zfin.marker.presentation;

import java.util.Comparator;

/**
 */
public class MarkerRelationshipSupplierComparator implements Comparator<MarkerRelationshipPresentation> {

    @Override
    public int compare(MarkerRelationshipPresentation mr1, MarkerRelationshipPresentation mr2) {
        int compare = mr1.getRelationshipType().compareTo(mr2.getRelationshipType());
        if (compare != 0) return compare;
        compare = mr1.getMarkerType().compareTo(mr2.getMarkerType());
        if (compare != 0) return compare;

        return mr1.getAbbreviationOrder().compareTo(mr2.getAbbreviationOrder());
    }
}
