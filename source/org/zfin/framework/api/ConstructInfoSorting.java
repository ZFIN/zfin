package org.zfin.framework.api;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.ConstructInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class ConstructInfoSorting implements Sorting<ConstructInfo> {

    private List<Comparator<ConstructInfo>> defaultList;
    private List<Comparator<ConstructInfo>> regRegionList;

    public ConstructInfoSorting() {
        super();

        defaultList = new ArrayList<>(3);
        defaultList.add(markerOrder);

        regRegionList = new ArrayList<>(3);
        regRegionList.add(regRegionOrder);

    }

/*
    private static Comparator<ConstructInfo> lengthOrder =
            Comparator.comparing(dbLink -> {
                if (dbLink.getLength() != null)
                    return (dbLink.getLength()) * (-1);
                return 0;
            });
*/

    private static Comparator<ConstructInfo> markerOrder =
            Comparator.comparing(info -> info.getConstruct().getAbbreviation());

    private static Comparator<ConstructInfo> regRegionOrder =
            Comparator.comparing(info -> {
                if (CollectionUtils.isNotEmpty(info.getRegulatoryRegions()))
                    return info.getRegulatoryRegions().stream()
                            .sorted(Comparator.comparing(Marker::getAbbreviation))
                            .map(Marker::getAbbreviation)
                            .collect(joining(","))
                            ;
                return null;
            });

    public Comparator<ConstructInfo> getComparator(String value) {
        Field field = Field.getField(value);
        if (field == null)
            return getJoinedComparator(defaultList);
        //throw new RuntimeException("Cannot find a sorting algorithm for name: " + value);

        switch (field) {
            case REGULATORY_REGIONS:
                return getJoinedComparator(regRegionList);
            default:
                return getJoinedComparator(defaultList);
        }
    }

    enum Field {
        REGULATORY_REGIONS("regulatoryRegion"), CODING_SEQUENCE("codingSequence"), SPECIES("species"), CITATION("citation");

        private String val;

        Field(String value) {
            this.val = value;
        }

        public static Field getField(String value) {
            return Arrays.stream(values()).filter(field -> field.getVal().equals(value)).findFirst().orElse(null);
        }

        public String getVal() {
            return val;
        }
    }
}
