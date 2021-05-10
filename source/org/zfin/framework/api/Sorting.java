package org.zfin.framework.api;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public interface Sorting<T> {

    default Comparator<T> getJoinedComparator(List<Comparator<T>> comparatorList) {
        if (comparatorList.isEmpty())
            return null;
        Comparator<T> joinedComparator = comparatorList.get(0);
        comparatorList.remove(0);
        for (Comparator<T> comparator : comparatorList) {
            joinedComparator = joinedComparator.thenComparing(comparator);
        }
        return joinedComparator;
    }

    // the last number in a string gets padded with zeros
    static String getSmartKey(String symbol) {
        String[] parts = symbol.split("(?=\\d+$)", 2);
        if (parts.length == 1)
            return symbol.toLowerCase();
        int num = Integer.parseInt(parts[1]);
        // make an 8 digit number padding with a number or zeros as needed
        final String s = parts[0].toLowerCase() + String.format("%08d", num);
        return s;
    }

    Comparator<T> getComparator(String name);



/*
    default SortingField getSortingField() {
        return Arrays.stream()
                .filter(sortingField -> {
                    if (sortingField.name().equalsIgnoreCase(name))
                        return true;
                    // allow snake case to be recognized as well
                    // e.g. genetic_entity ~= geneticEntity
                    return sortingField.name().replace("_", "").equalsIgnoreCase(name);
                })
                .findFirst()
                .orElse(null);
    }
*/

}