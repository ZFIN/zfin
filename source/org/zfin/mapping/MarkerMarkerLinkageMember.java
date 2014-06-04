package org.zfin.mapping;

import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;

/**
 * Created by cmpich on 3/4/14.
 */
public class MarkerMarkerLinkageMember extends LinkageMember {

    private Marker marker;
    private Marker pairedMarker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getPairedMarker() {
        return pairedMarker;
    }

    public void setPairedMarker(Marker pairedMarker) {
        this.pairedMarker = pairedMarker;
        entityOne = marker;
        entityTwo = pairedMarker;
    }

    @Override
    public ZdbID getLinkedMember() {
        return pairedMarker;
    }

    @Override
    public LinkageMember getInverseMember() {
        MarkerMarkerLinkageMember inverse;
        try {
            inverse = (MarkerMarkerLinkageMember) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
        inverse.setMarker(pairedMarker);
        inverse.setPairedMarker(marker);
        return inverse;
    }
}
