package org.zfin.framework.api;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.feature.Feature;
import org.zfin.marker.presentation.Prioritization;
import org.zfin.marker.presentation.Prioritization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.joining;

public class PubGeneSorting implements Sorting<Prioritization> {

    private List<Comparator<Prioritization>> defaultList;
    private List<Comparator<Prioritization>> symbolList;
    private List<Comparator<Prioritization>> expDataList;
    private List<Comparator<Prioritization>> phenoDataList;

    public PubGeneSorting() {
        super();

        defaultList = new ArrayList<>(3);
        defaultList.add(symbolOrder);


        symbolList = new ArrayList<>(3);
        symbolList.add(symbolOrder);


        expDataList = new ArrayList<>(3);
        expDataList.add(expDataOrder);
        expDataList.add(symbolOrder);

        phenoDataList = new ArrayList<>(3);
        phenoDataList.add(phenoDataOrder);
        phenoDataList.add(symbolOrder);

    }

    private static Comparator<Prioritization> symbolOrder =
            Comparator.comparing(info -> info.getName());

    private static Comparator<Prioritization> expDataOrder =
            Comparator.comparing(info -> info.getMarkerExpression().getExpressionFigureCount());

    private static Comparator<Prioritization> phenoDataOrder =
            Comparator.comparing(info -> info.getPhenoOnMarker().getNumFigures());

    public Comparator<Prioritization> getComparator(String value) {
        Field field = Field.getField(value);
        if (field == null)
            return getJoinedComparator(defaultList);
        //throw new RuntimeException("Cannot find a sorting algorithm for name: " + value);

        switch (field) {
            case SYMBOL_UP:
                return getJoinedComparator(defaultList);
            case SYMBOL_DOWN:
                return getJoinedComparator(defaultList).reversed();
            case EXPDATA_UP:
                return getJoinedComparator(expDataList);
            case EXPDATA_DOWN:
                return getJoinedComparator(expDataList).reversed();
            case PHENODATA_UP:
                return getJoinedComparator(phenoDataList);
            case PHENODATA_DOWN:
                return getJoinedComparator(phenoDataList).reversed();
            default:
                return getJoinedComparator(defaultList);
        }
    }

    enum Field {
        SYMBOL_UP("symbolUp"), SYMBOL_DOWN("strDown"),
        EXPDATA_UP("expDataUp"), EXPDATA_DOWN("expDataDown"), PHENODATA_UP("phenoDataUp"), PHENODATA_DOWN("phenoDataDown");

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
