package org.zfin.framework.api;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SortingField {

    ;

/*
    public static SortingField getSortingField(String name) {
        return Arrays.stream(values())
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

    public static boolean isValidSortingFieldValue(String value) {
        if (value == null || StringUtils.isBlank(value))
            return true;
        return getSortingField(value) != null;
    }

    public List<SortingField> getAllValues() {
        return Arrays.stream(values()).collect(Collectors.toList());
    }
*/
}
