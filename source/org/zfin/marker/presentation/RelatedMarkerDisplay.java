package org.zfin.marker.presentation;

import org.zfin.marker.MarkerType;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Presentation class to hold grouped and sorted data for marker relationship display
 */
public class RelatedMarkerDisplay extends TreeMap<String, TreeMap<MarkerType, TreeSet<RelatedMarker>>> {

    public void addRelatedMarker(RelatedMarker relatedMarker) {

        if (relatedMarker != null) {
            String label = relatedMarker.getLabel();
            MarkerType markerType = relatedMarker.getMarker().getMarkerType();
            TreeMap<MarkerType, TreeSet<RelatedMarker>> typeMap;
            TreeSet<RelatedMarker> relatedMarkers;

            //if the label isn't in the hash yet, we need to add the full
            //relationship->(markertype->list) kaboodle.
            if (!this.containsKey(label)) {
                typeMap = new TreeMap<MarkerType, TreeSet<RelatedMarker>>();
                this.put(label, typeMap);
                relatedMarkers = new TreeSet<RelatedMarker>();
                relatedMarkers.add(relatedMarker);
                typeMap.put(markerType, relatedMarkers);
            } else {
                //we already have the relationship, see if we already have the type
                //as well
                typeMap = this.get(label);
                //we don't have a map for this markertype, add one
                if (!typeMap.containsKey(markerType)) {
                    relatedMarkers = new TreeSet<RelatedMarker>();
                    relatedMarkers.add(relatedMarker);
                    typeMap.put(markerType, relatedMarkers);
                } else {
                    //all of the hashes and lists already exists, so just add the marker
                    relatedMarkers = typeMap.get(markerType);
                    relatedMarkers.add(relatedMarker);
                }

            }

        }


    }

}
