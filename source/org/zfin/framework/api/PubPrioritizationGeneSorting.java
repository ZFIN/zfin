package org.zfin.framework.api;

import org.zfin.marker.presentation.Prioritization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PubPrioritizationGeneSorting implements Sorting<Prioritization> {

    private List<Comparator<Prioritization>> defaultList;
    private List<Comparator<Prioritization>> expressionDataList;
    private List<Comparator<Prioritization>> phenoDataList;

    public PubPrioritizationGeneSorting() {
        super();

        defaultList = new ArrayList<>(1);
        defaultList.add(symbolOrder);

        expressionDataList = new ArrayList<>(2);
        expressionDataList.add(expDataOrder);
        expressionDataList.add(symbolOrder);

        phenoDataList = new ArrayList<>(2);
        phenoDataList.add(phenoDataOrder);
        phenoDataList.add(symbolOrder);

    }

    private static Comparator<Prioritization> symbolOrder =
            Comparator.comparing(Prioritization::getName, String.CASE_INSENSITIVE_ORDER);

    private static Comparator<Prioritization> expDataOrder =
            Comparator.comparingInt(Prioritization::getExpressionFigures);

    private static Comparator<Prioritization> phenoDataOrder =
            Comparator.comparingInt(Prioritization::getPhenotypeFigures);

    public Comparator<Prioritization> getComparator(String value) {
        Field field = Field.getField(value);
        if (field == null)
            return getJoinedComparator(defaultList);

        switch (field) {
            case SYMBOL_UP:
                return getJoinedComparator(defaultList);
            case SYMBOL_DOWN:
                return getJoinedComparator(defaultList).reversed();
            case EXPDATA_UP:
                return getJoinedComparator(expressionDataList);
            case EXPDATA_DOWN:
                return getJoinedComparator(expressionDataList).reversed();
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
