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

    /**
     * Adds items from the itemsToAdd list to the existingList if they are not already contained in it,
     * based on the specified key function.
     *
     * The uniqueness of items is determined by applying the keyFunction to each item.
     * If the key value of an item from itemsToAdd is not found in the key values of existingList,
     * then the item will be added to existingList.
     *
     *
     * @param <T>           the type of the items in the lists
     * @param existingList  the list to which items may be added
     * @param itemsToAdd    the list of items to consider adding to {@code existingList}
     * @param keyFunction   a function to produce a key value for an item, used to determine uniqueness
     */
    public static <T> void addToListIfNotContainsBy(List<T> existingList, List<T> itemsToAdd, Function<T, ?> keyFunction) {
        itemsToAdd.forEach(item -> {
            if (!ZfinCollectionUtils.containsBy(existingList, item, keyFunction)) {
                existingList.add(item);
            }
        });
    }

}



