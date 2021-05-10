package org.zfin.mapping;

import org.zfin.marker.Marker;

/**
 *
 */
public class MarkerSingletonLinkage extends SingletonLinkage {

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        entity = marker;
    }
}
