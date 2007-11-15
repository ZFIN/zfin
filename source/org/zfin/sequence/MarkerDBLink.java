package org.zfin.sequence;

import org.zfin.marker.Marker;

public class MarkerDBLink extends DBLink {

    private Marker marker;
//    private Accession referencingAccession ;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    
}
