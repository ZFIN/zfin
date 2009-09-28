package org.zfin.mutant;

import org.zfin.marker.Marker;

/**
 */
public class MarkerGoTermEvidence {
    private String zdbID ;
    private Marker marker ;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
