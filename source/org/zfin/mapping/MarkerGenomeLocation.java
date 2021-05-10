package org.zfin.mapping;

import org.zfin.marker.Marker;

/**
 * Genomic location info for a marker.
 */
public class MarkerGenomeLocation extends GenomeLocation {

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        this.entityID = marker.getZdbID();
    }
}
