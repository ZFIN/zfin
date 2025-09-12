package org.zfin.util;

import java.util.*;
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
     * Alias for firstInEachGrouping.
     */
    public static <T, K> List<T> uniqueBy(List<T> inputs, Function<T, K> groupingFunction) {
        return firstInEachGrouping(inputs, groupingFunction);
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

    /**
     * Removes entries from the input map that have duplicate values and returns a map of those duplicate values
     * along with the set of keys that had those values.
     *
     * example:
     * Input: {A=1, B=2, C=1, D=3, E=2}
     * Output: {1={A, C}, 2={B, E}}
     * The original map will be modified to: {D=3}
     *
     * @param inputMap the original map from which to remove duplicates
     * @return a map where each key is a duplicate value from the original map, and the corresponding value is a set
     *         of all keys that had that duplicate value
     */
    public static Map<String, Set<String>> removeAndReturnDuplicateMapEntries(Map<String, String> inputMap) {
        // Map to track value -> set of keys that have that value
        Map<String, Set<String>> valueToKeys = new HashMap<>();

        // First pass: build the value to keys mapping
        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            valueToKeys.computeIfAbsent(value, v -> new HashSet<>()).add(key);
        }

        // Map to return (contains only duplicate values)
        Map<String, Set<String>> duplicates = new HashMap<>();

        // Second pass: identify duplicates and remove ALL of them from original map
        Iterator<Map.Entry<String, String>> iterator = inputMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String value = entry.getValue();
            Set<String> keysWithThisValue = valueToKeys.get(value);

            // If this value appears more than once, it's a duplicate
            if (keysWithThisValue.size() > 1) {
                duplicates.put(value, new HashSet<>(keysWithThisValue));

                // Remove this entry from the original map (remove ALL duplicates)
                iterator.remove();
            }
        }

        return duplicates;
    }

}



