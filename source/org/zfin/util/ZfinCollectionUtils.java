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
     *
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

    /**
     * Determines whether the provided list contains an element based on a specific attribute/key.
     *
     * @param <T> The type of elements in the list.
     * @param list The list to search within.
     * @param element The element containing the key/attribute to search for.
     * @param keyFunction A function that extracts the key/attribute from an element of type T.
     * @return {@code true} if the list contains an element with the specified key/attribute,
     *         {@code false} otherwise.
     */
    public static <T> boolean containsBy(List<T> list, T element, Function<T, ?> keyFunction) {
        return list.stream()
                .map(keyFunction)
                .anyMatch(key -> key.equals(keyFunction.apply(element)));
    }

}



