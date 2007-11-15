package org.zfin.util;

import java.util.List;
import java.util.Arrays;

/**
 * User: giles
 * Date: Aug 3, 2006
 * Time: 4:02:02 PM
 */

public class FilterType {
    private String name;

    public static final FilterType CONTAINS = new FilterType("contains");
    public static final FilterType BEGINS = new FilterType("begins");
    public static final FilterType ENDS = new FilterType("ends");
    public static final FilterType EQUALS = new FilterType("equals");
    public static final FilterType LIST = new FilterType("list");
    public static final FilterType RANGE = new FilterType("range");

    private static final FilterType[] symbolFilters = {CONTAINS, EQUALS, BEGINS, ENDS};
    private static final FilterType[] chromosomeFilters = {EQUALS, LIST, RANGE};
    private static final FilterType[] positionFilters = {EQUALS, BEGINS, RANGE};
    private static final FilterType[] completeList = {CONTAINS, BEGINS, ENDS, EQUALS, LIST, RANGE};

    private FilterType(String type) {
        this.name = type;
    }

    public String getName() {
        return name;
    }

    public static FilterType getFilterType(String name) {
        for (int i = 0; i < completeList.length; i++) {
            FilterType currentFilter = completeList[i];
            if (currentFilter.getName().equals(name)) {
                return currentFilter;
            }
        }
        throw new RuntimeException("Could not find filtertype for string: " + name);
    }

    public static List<FilterType> getSymbolFilters() {
        return Arrays.asList(symbolFilters);
    }

    public static List<FilterType> getChromosomeFilters() {
        return Arrays.asList(chromosomeFilters);
    }

    public static List<FilterType> getPositionFilters() {
        return Arrays.asList(positionFilters);
    }
}
