package org.zfin.util;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;

/**
 * Utilities that are not in CollectionUtils.
 */
public class ZfinCollectionUtils {

    /**
     * Given a list of objects and a grouping function, return a list of the first object in each group.
     * @param inputs
     * @param groupingFunction
     * @return list of the first object in each group
     * @param <T>
     * @param <K>
     */
    public static <T, K> List<T> firstInEachGrouping(List<T> inputs, Function<T, K> groupingFunction) {
        return inputs.stream()
                .collect(groupingBy(groupingFunction))
                .values()
                .stream()
                .map(group -> group.get(0))
                .toList();
    }
}



