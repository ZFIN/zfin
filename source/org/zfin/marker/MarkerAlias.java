package org.zfin.marker;

import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.PublicationAttribution;

public class MarkerAlias extends DataAlias {
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

}

