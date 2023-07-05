package org.zfin.stats;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Range;
import org.zfin.antibody.Antibody;
import org.zfin.infrastructure.ZdbID;
import org.zfin.infrastructure.ZfinID;
import org.zfin.publication.Publication;

import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

public class StatisticServiceHelper<Entity extends ZdbID> {


    private <ID> Range getCardinality(Map<String, List<Entity>> alleleMap, Function<Entity, List<ID>> function) {
        Map<String, Integer> cardinality = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .collect(toMap(ZdbID::getZdbID,
                        o -> function.apply(o).size(),
                        // if duplicates occur just take one.
                        (a1, a2) -> a1));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private Range getCardinalityPerUniqueRow(Map<Publication, List<Entity>> publicationMap) {
        Map<String, Integer> cardinality = publicationMap.entrySet().stream()
                .collect(toMap(entry -> entry.getKey().getZdbID(), entry -> entry.getValue().size()));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private static <ID> Range getCardinalityPerUniqueRow(Collection<List<ZfinID>> map, Function<ZfinID, List<ID>> function) {
        Map<String, Integer> cardinality = map.stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(toMap(ZfinID::getZdbID,
                        o -> {
                            List<ID> apply = function.apply(o);
                            return apply == null ? 0 : apply.size();
                        },
                        // if duplicates occur just take one.
                        (a1, a2) -> a1));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    public int getTotalNumber(Collection<List<Entity>> map) {
        return map.stream()
                .mapToInt(List::size).sum();
    }

    private static int getTotalNumberBase(List<ZfinID> list, Function<ZfinID, Object> function) {
        return (int) list.stream()
                .filter(o -> function.apply(o) != null)
                .count();
    }

    private static int getTotalNumberMultiValued(Collection<List<ZfinID>> map, Function<ZfinID, List<String>> function) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(function)
                .mapToInt(List::size).sum();
    }

    private static int getTotalNumberMultiValuedBase(List<ZfinID> map, Function<ZfinID, List<? extends Object>> function) {
        return map.stream()
                .map(function)
                .mapToInt(List::size).sum();
    }

    private static <ID> int getTotalNumberMultiValuedObject(Collection<List<ZfinID>> map, Function<ZfinID, List<ID>> function) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(function)
                .filter(Objects::nonNull)
                .mapToInt(List::size).sum();
    }

    /*
        private static <ID> int getTotalNumberPerUberEntity(List<Allele> alleles, Function<Allele, List<ID>> function) {
            return alleles.stream()
                    .map(function)
                    .mapToInt(List::size).sum();
        }
    */
    public <T extends ZdbID> long getTotalDistinctNumber(Collection<List<T>> map) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(ZdbID::getZdbID)
                .distinct()
                .count();
    }

    private static <ID> Long getTotalDistinctNumber(Collection<List<ZfinID>> map, Function<ZfinID, List<ID>> function) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(function)
                .flatMap(Collection::stream)
                .distinct()
                .count();
    }

    private static <ID> long getTotalDistinctNumberBase(List<ZfinID> map, Function<ZfinID, List<ID>> function) {
        return map.stream()
                .map(function)
                .distinct()
                .count();
    }

    private static long getTotalDistinctNumberOnObjectBase(List<ZfinID> map, Function<ZfinID, Object> function) {
        return map.stream()
                .map(function)
                .distinct()
                .count();
    }

    private static long getTotalDistinctNumberOnObject(Collection<List<ZfinID>> map, Function<ZfinID, Object> function) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(function)
                .distinct()
                .count();
    }

    private static <ID> int getTotalDistinctNumberPerUberEntity(List<ZfinID> alleles, Function<ZfinID, List<ID>> function) {
        return alleles.stream()
                .map(function)
                .flatMap(Collection::stream)
                .collect(toSet())
                .size();
    }

    private static Map<String, Integer> getHistogram(Map<Publication, List<ZfinID>> alleleMap, Function<ZfinID, String> function) {
        Map<String, List<String>> histogramRaw = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(function)
                .collect(toList())
                .stream()
                .collect(groupingBy(o -> {
                    if (o == null)
                        return "<empty>";
                    return o;
                }));
        return getValueSortedMap(histogramRaw);
    }

    private static Map<String, Integer> getHistogramMultiValued(Map<Publication, List<ZfinID>> alleleMap, Function<ZfinID, List<String>> function) {
        Map<String, List<String>> histogramRaw = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(function)
                .flatMap(Collection::stream)
                .collect(toList())
                .stream()
                .collect(groupingBy(o -> {
                    if (o == null)
                        return "";
                    return o;
                }));
        return getValueSortedMap(histogramRaw);
    }

    private static Map<String, Integer> getHistogramMultiValuedOnUniqueRow(Collection<List<ZfinID>> antibodies, Function<ZfinID, List<String>> function) {
        Map<String, List<String>> histogramRaw = antibodies.stream()
                .flatMap(Collection::stream)
                .map(function)
                .flatMap(Collection::stream)
                .collect(toList())
                .stream()
                .collect(groupingBy(o -> {
                    if (o == null)
                        return "<empty>";
                    return o;
                }));
        return getValueSortedMap(histogramRaw);
    }

    private static Map<String, Integer> getValueSortedMap(Map<String, List<String>> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 0)
                .sorted(comparingInt(entry -> -entry.getValue().size()))
                .collect(toMap(
                        Map.Entry::getKey,
                        o -> o.getValue().size(),
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
    }

    /*
        private String getMultiValues(List<Allele> alleles, Function<Allele, ? extends List<?>> multiValueFunction) {
            if (CollectionUtils.isEmpty(alleles) || alleles.size() > 1)
                return null;
            List<?> values = multiValueFunction.apply(alleles.get(0));
            return values.stream().map(Object::toString).collect(joining(", "));
        }


    */
    private ColumnStats getRowEntityColumn() {
        final Optional<ColumnStats<Antibody, ?>> any = stats.stream().filter(ColumnStats::isRowEntity).findAny();
        if (any.isEmpty())
            throw new RuntimeException("Missing row entity column");
        return any.get();
    }

    private List<ColumnStats<Antibody, ?>> stats;

/*

    private List<ColumnStats<Allele, ?>> getSubEntityRowColumn() {
        return stats.stream()
                .filter(columnStats -> !columnStats.isRowEntity())
                .filter(columnStats -> !columnStats.isSuperEntity())
                .collect(toList());
    }

    private List<ColumnStats<Allele, ?>> stats;

    public void add(List<ColumnStats<Allele, ?>> stats) {
        this.stats = stats;
    }

*/
}
