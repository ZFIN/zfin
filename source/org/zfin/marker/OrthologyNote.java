package org.zfin.marker;

import org.zfin.ExternalNote;
import org.zfin.infrastructure.PublicationAttribution;

/**
 * Note entered by Curators concerning the existence or absence of orthology.
 */
public class OrthologyNote extends ExternalNote {

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

}