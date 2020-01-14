package org.zfin.framework.api;

import org.zfin.marker.presentation.RelatedMarker;

public class MarkerRelationshipFiltering extends Filtering<RelatedMarker> {


    public MarkerRelationshipFiltering() {
        filterFieldMap.put(FieldFilter.RELATIONSHIP_TYPE, typeFilter);
    }
    public static FilterFunction<RelatedMarker, String> typeFilter =
            (relatedMarker, value) -> FilterFunction.contains(relatedMarker.getMarkerRelationshipType().getName().toLowerCase(), value);



}
