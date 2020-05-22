package org.zfin.framework.api;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.feature.Feature;
import org.zfin.marker.presentation.SequenceTargetingReagentBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.joining;

public class STRBeanSorting implements Sorting<SequenceTargetingReagentBean> {

    private List<Comparator<SequenceTargetingReagentBean>> defaultList;
    private List<Comparator<SequenceTargetingReagentBean>> alleleList;
    private List<Comparator<SequenceTargetingReagentBean>> citationList;

    public STRBeanSorting() {
        super();

        defaultList = new ArrayList<>(3);
        defaultList.add(markerOrder);
        defaultList.add(alleleOrder);

        alleleList = new ArrayList<>(3);
        alleleList.add(alleleOrder);
        alleleList.add(markerOrder);

        citationList = new ArrayList<>(3);
        citationList.add(citationOrder);
        citationList.add(markerOrder);

    }

    private static Comparator<SequenceTargetingReagentBean> markerOrder =
            Comparator.comparing(info -> info.getMarker().getAbbreviation());

    private static Comparator<SequenceTargetingReagentBean> citationOrder =
            Comparator.comparing(info -> info.getMarker().getPublications().size());

    private static Comparator<SequenceTargetingReagentBean> alleleOrder =
            Comparator.comparing(info -> {
                if (CollectionUtils.isNotEmpty(info.getGenomicFeatures()))
                    return info.getGenomicFeatures().stream()
                            .sorted(Comparator.comparing(Feature::getAbbreviation))
                            .map(Feature::getAbbreviation)
                            .collect(joining())
                            ;
                return null;
            }, Comparator.nullsLast(naturalOrder()));

    public Comparator<SequenceTargetingReagentBean> getComparator(String value) {
        Field field = Field.getField(value);
        if (field == null)
            return getJoinedComparator(defaultList);
        //throw new RuntimeException("Cannot find a sorting algorithm for name: " + value);

        switch (field) {
            case ALLELE_UP:
                return getJoinedComparator(alleleList);
            case ALLELE_DOWN:
                return getJoinedComparator(alleleList).reversed();
            case STR_UP:
                return getJoinedComparator(defaultList);
            case STR_DOWN:
                return getJoinedComparator(defaultList).reversed();
            case CITATION_UP:
                return getJoinedComparator(citationList);
            case CITATION_DOWN:
                return getJoinedComparator(citationList).reversed();
            default:
                return getJoinedComparator(defaultList);
        }
    }

    enum Field {
        STR_UP("strUp"), STR_DOWN("strDown"),
        ALLELE_UP("createdAlleleUp"), ALLELE_DOWN("createdAlleleDown"), CITATION_UP("citationMost"), CITATION_DOWN("citationLeast");

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
