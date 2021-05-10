package org.zfin.framework.api;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.feature.Feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class FeatureSorting implements Sorting<Feature> {

    private List<Comparator<Feature>> defaultList;
    private List<Comparator<Feature>> alleleList;
    private List<Comparator<Feature>> consequenceList;
    private List<Comparator<Feature>> typeList;
    private List<Comparator<Feature>> supplierList;

    public FeatureSorting() {
        super();

        defaultList = new ArrayList<>(3);
        defaultList.add(markerOrder);

        alleleList = new ArrayList<>(3);
        alleleList.add(markerOrder);

        consequenceList = new ArrayList<>(3);
        consequenceList.add(consequenceOrder);

        typeList = new ArrayList<>(3);
        typeList.add(typeOrder);
        typeList.add(markerOrder);

        supplierList = new ArrayList<>(3);
        supplierList.add(supplierOrder);
        supplierList.add(markerOrder);

    }

    private static Comparator<Feature> markerOrder =
            Comparator.comparing(Feature::getAbbreviation);

    private static Comparator<Feature> supplierOrder =
            Comparator.comparing(feature -> {
                if (CollectionUtils.isEmpty(feature.getSuppliers()))
                    return null;
                return feature.getSuppliers().stream()
                        .map(featureSupplier -> featureSupplier.getOrganization().getName())
                        .sorted(Comparator.naturalOrder())
                        .collect(joining());
            }, Comparator.nullsLast(Comparator.naturalOrder()));

    private static Comparator<Feature> typeOrder =
            Comparator.comparing(feature -> feature.getType().getDisplay());

    private static Comparator<Feature> consequenceOrder =
            Comparator.comparing(Feature::getTranscriptConsequenceStatement, Comparator.nullsLast(Comparator.naturalOrder()));

    public Comparator<Feature> getComparator(String value) {
        Field field = Field.getField(value);
        if (field == null)
            return getJoinedComparator(defaultList);
        //throw new RuntimeException("Cannot find a sorting algorithm for name: " + value);

        switch (field) {
            case ALLELE_UP:
                return getJoinedComparator(alleleList);
            case CONSEQUENCE_UP:
                return getJoinedComparator(consequenceList);
            case CONSEQUENCE_DOWN:
                return getJoinedComparator(consequenceList).reversed();
            case TYPE_UP:
                return getJoinedComparator(typeList);
            case TYPE_DOWN:
                return getJoinedComparator(typeList).reversed();
            case SUPPLIER_UP:
                return getJoinedComparator(supplierList);
            default:
                return getJoinedComparator(defaultList);
        }
    }

    enum Field {
        TYPE_UP("typeUp"), TYPE_DOWN("typeDown"),
        CONSEQUENCE_UP("consequenceUp"), CONSEQUENCE_DOWN("consequenceDown"),
        SUPPLIER_UP("supplierUp"),
        ALLELE_UP("createdAlleleUp");

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
