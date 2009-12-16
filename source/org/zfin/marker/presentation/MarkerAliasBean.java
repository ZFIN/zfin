package org.zfin.marker.presentation;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.MarkerAlias;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;

import java.util.HashSet;
import java.util.Set;


public class MarkerAliasBean extends PublicationListBean {

    private MarkerAlias markerAlias;

    public MarkerAlias getMarkerAlias() {
        if (markerAlias == null) {
            markerAlias = new MarkerAlias();
        }
        return markerAlias;
    }

    public void setMarkerAlias(MarkerAlias markerAlias) {
        this.markerAlias = markerAlias;
    }

    public Set<Publication> getPublications() {
        Set<Publication> publications = new HashSet<Publication>();

        if (markerAlias == null)
            return publications;

        Set<PublicationAttribution> pubAttributions = markerAlias.getPublications();
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