package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;

import java.util.Set;

public class SNPBean extends PublicationListBean {
    private Marker marker;
    private String markerID;
    private Set<Publication> publications;

    public Marker getMarker() {
        if (marker == null)
            return new Marker();
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public String getMarkerID() {
        return markerID;
    }

    public void setMarkerID(String markerID) {
        this.markerID = markerID;
    }
}
