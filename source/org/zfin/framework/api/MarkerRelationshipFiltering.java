package org.zfin.framework.api;

import org.zfin.marker.presentation.MarkerRelationshipPresentation;

public class MarkerRelationshipFiltering extends Filtering<MarkerRelationshipPresentation> {


    public MarkerRelationshipFiltering() {
        filterFieldMap.put(FieldFilter.RELATIONSHIP_TYPE, typeFilter);
    }
    public static FilterFunction<MarkerRelationshipPresentation, String> typeFilter =
            (relatedMarker, value) -> FilterFunction.contains(relatedMarker.getMappedMarkerRelationshipType().toLowerCase(), value);



}
