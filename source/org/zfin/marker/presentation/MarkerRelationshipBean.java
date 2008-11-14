package org.zfin.marker.presentation;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.MarkerRelationship;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;

import java.util.HashSet;
import java.util.Set;


public class MarkerRelationshipBean extends PublicationListBean {

    private MarkerRelationship markerRelationship;

    public MarkerRelationship getMarkerRelationship() {
        if (markerRelationship == null)
            markerRelationship = new MarkerRelationship();
        return markerRelationship;
    }

    public void setMarkerRelationship(MarkerRelationship markerRelationship) {
        this.markerRelationship = markerRelationship;
    }

    public Set<Publication> getPublications() {
        Set<Publication> publications = new HashSet<Publication>();

        if (markerRelationship == null)
            return publications;

        Set<PublicationAttribution> pubAttributions = markerRelationship.getPublications();
        if (pubAttributions != null && !pubAttributions.isEmpty()) {
            for (PublicationAttribution attr : pubAttributions) {
                Publication pub = attr.getPublication();
                if (pub != null)
                    publications.add(pub);
            }
        }

        return publications;
    }

}