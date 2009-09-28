package org.zfin.marker;

import org.zfin.infrastructure.DataAlias;

public class MarkerAlias extends DataAlias {
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}

