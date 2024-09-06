package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.sequence.TranscriptDBLink;

import java.util.List;

/**
 * A helpful abstraction from the mapped marker relationship object,
 * contains the related marker and the marker relationship type
 */
@Data
public class RelatedMarker implements Comparable {
    @JsonView(View.MarkerRelationshipAPI.class)
    private Marker marker;
    @JsonView(View.MarkerRelationshipAPI.class)
    MarkerRelationshipType markerRelationshipType;
    @JsonView(View.MarkerRelationshipAPI.class)
    private MarkerRelationship markerRelationship;
    private String label;
    private List<TranscriptDBLink> displayedSequenceDBLinks; // if a marker has ONE associated sequence

    private boolean hasHavanna;

    public void setMarkerRelationshipType(MarkerRelationshipType markerRelationshipType) {
        this.markerRelationshipType = markerRelationshipType;
    }





    public RelatedMarker() { }

    /**
     * Create a relatedMarker object by passing in the current marker
     * and the relationship.  The point of this class is to hide the
     * details about whether the marker of interest is in the 1st or
     * 2nd position within the relationship.
     * @param thisMarker marker of interest
     * @param mrel relationship containing thisMarker in position 1 or 2
     */
    public RelatedMarker(Marker thisMarker, MarkerRelationship mrel) {
        if (thisMarker.equals(mrel.getFirstMarker())) {
            setMarker(mrel.getSecondMarker());
            setMarkerRelationship(mrel);
            setLabel(MarkerPresentation.getAbbreviation(mrel.getFirstMarker()) + " " + mrel.getMarkerRelationshipType().getFirstToSecondLabel());
        } else if (thisMarker.equals(mrel.getSecondMarker())) {
            setMarker(mrel.getFirstMarker());
            setMarkerRelationship(mrel);
            setLabel(MarkerPresentation.getAbbreviation(mrel.getSecondMarker()) + " " + mrel.getMarkerRelationshipType().getSecondToFirstLabel());
        }
     }

    /**
     * Create a RelatedMarker with a specific label.  (used for creating relationships in
     * the display layer that don't actually exist in the database)
     * @param thisMarker  Marker to realte.
     * @param mrel  Marker relationship.
     * @param label  Label to add.
     */
    public RelatedMarker(Marker thisMarker, MarkerRelationship mrel, String label) {
        if (thisMarker.equals(mrel.getFirstMarker())) {
            setMarker(mrel.getSecondMarker());
            setMarkerRelationship(mrel);
            setLabel(label);
        } else if (thisMarker.equals(mrel.getSecondMarker())) {
            setMarker(mrel.getFirstMarker());
            setMarkerRelationship(mrel);
            setLabel(label);
        }
     }

    public MarkerRelationshipType getMarkerRelationshipType() {
        return markerRelationship.getMarkerRelationshipType();
    }

    public List<TranscriptDBLink> getDisplayedSequenceDBLinks() {
        return displayedSequenceDBLinks;
    }

    public void setDisplayedSequenceDBLinks(List<TranscriptDBLink> displayedSequenceDBLinks) {
        this.displayedSequenceDBLinks = displayedSequenceDBLinks;
    }

    public Marker getOtherMarker() {
        if (marker.equals(markerRelationship.getFirstMarker()))
            return markerRelationship.getSecondMarker();
        else return markerRelationship.getFirstMarker();
    }

    public int compareTo(Object o) {
        RelatedMarker rm = (RelatedMarker) o;
        if (rm == null) { return +1; }
        return getMarker().compareTo(rm.getMarker());
    }

}
