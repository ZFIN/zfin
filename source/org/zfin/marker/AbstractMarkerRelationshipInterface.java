package org.zfin.marker;

import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;


/**
 * interface MarkerRelationship.
 */
public interface AbstractMarkerRelationshipInterface {

    String getZdbID();

    void setZdbID(String zdbID);

    MarkerRelationship.Type getType();

    void setType(MarkerRelationship.Type type);

    MarkerRelationshipType getMarkerRelationshipType();

    void setMarkerRelationshipType(MarkerRelationshipType markerRelationshipType);

    Marker getFirstMarker();

    void setFirstMarker(Marker firstMarker);

    Marker getSecondMarker();

    void setSecondMarker(Marker secondMarker);

    Set<PublicationAttribution> getPublications();

    void setPublications(Set<PublicationAttribution> publications);
}


