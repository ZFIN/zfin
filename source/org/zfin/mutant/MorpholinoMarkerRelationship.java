package org.zfin.mutant;

import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;


/**
 * Class MorpholinoMarkerRelationship.
 */
public class MorpholinoMarkerRelationship extends MarkerRelationship {

    private Morpholino firstMarker;

    public Morpholino getFirstMarker() {
        return firstMarker;
    }

    public void setFirstMarker(Morpholino firstMarker) {
        this.firstMarker = firstMarker;
    }
}


