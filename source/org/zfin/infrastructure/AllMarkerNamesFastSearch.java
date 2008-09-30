package org.zfin.infrastructure;

import org.zfin.marker.Marker;

/**
 * Fast Search convenience class for markers.
 */
public class AllMarkerNamesFastSearch extends AllNamesFastSearch {

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
