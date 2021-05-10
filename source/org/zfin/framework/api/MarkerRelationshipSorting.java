package org.zfin.framework.api;

import org.zfin.marker.presentation.MarkerRelationshipPresentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MarkerRelationshipSorting implements Sorting<MarkerRelationshipPresentation> {

    private List<Comparator<MarkerRelationshipPresentation>> defaultList;

    public MarkerRelationshipSorting() {
        super();

        defaultList = new ArrayList<>(3);
        defaultList.add(relationshipTypeOrder);
    }


    private static Comparator<MarkerRelationshipPresentation> relationshipTypeOrder =
            Comparator.comparing(relatedMarker -> relatedMarker.getMappedMarkerRelationshipType());

    public Comparator<MarkerRelationshipPresentation> getComparator(String value) {
        Field field = Field.getField(value);
        if (field == null)
            return getJoinedComparator(defaultList);
        //throw new RuntimeException("Cannot find a sorting algorithm for name: " + value);

        switch (field) {
/*
            case DEFAULT:
                return getJoinedComparator(defaultList);
*/
            default:
                return getJoinedComparator(defaultList);
        }
    }

    enum Field {
        RELATIONSHIP_TYPE;

        public static Field getField(String value) {
            return Arrays.stream(values()).filter(field -> field.name().equals(value)).findFirst().orElse(null);
        }
    }
}

