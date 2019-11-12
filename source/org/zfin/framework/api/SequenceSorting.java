package org.zfin.framework.api;

import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SequenceSorting implements Sorting<MarkerDBLink> {

    private List<Comparator<MarkerDBLink>> defaultList;

    public SequenceSorting() {
        super();

        defaultList = new ArrayList<>(3);
        defaultList.add(markerTypeOrder);
        defaultList.add(markerAbbreviationOrder);
        defaultList.add(sequenceTypeOrder);
        defaultList.add(foreignDBSignificanceOrder);
        defaultList.add(lengthOrder);
        defaultList.add(accessionOrder);


    }

    private static Comparator<MarkerDBLink> lengthOrder =
            Comparator.comparing(dbLink -> {
                if (dbLink.getLength() != null)
                    return (dbLink.getLength()) * (-1);
                return 0;
            });

    private static Comparator<MarkerDBLink> markerTypeOrder =
            Comparator.comparing(dbLink -> dbLink.getMarker().getMarkerType().getName());

    private static Comparator<MarkerDBLink> markerAbbreviationOrder =
            Comparator.comparing(dbLink -> dbLink.getMarker().getAbbreviationOrder());

    private static Comparator<MarkerDBLink> sequenceTypeOrder =
            Comparator.comparing(dbLink -> dbLink.getReferenceDatabase().getForeignDBDataType().getDisplayOrder());

    private static Comparator<MarkerDBLink> accessionOrder =
            Comparator.comparing(DBLink::getAccessionNumberDisplay);

    private static Comparator<MarkerDBLink> foreignDBSignificanceOrder =
            Comparator.comparing(markerDBLink -> markerDBLink.getReferenceDatabase().getForeignDB().getSignificance());

    public Comparator<MarkerDBLink> getComparator(String value) {
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
        LENGTH, ACCESSION;

        public static Field getField(String value) {
            return Arrays.stream(values()).filter(field -> field.name().equals(value)).findFirst().orElse(null);
        }
    }
}
