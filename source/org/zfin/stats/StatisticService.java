package org.zfin.stats;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Transcript;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

public class StatisticService<Entity extends EntityZdbID, SubEntity extends EntityZdbID> {


    protected Map<Entity, Integer> getSortedEntityMap(Map<Entity, List<SubEntity>> geneMap) {
        geneMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));
        HashMap<Entity, Integer> integerMap;
        // default sorting: number of entities
        integerMap = geneMap.entrySet().stream()
            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        return integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }


    private ColumnValues getColumnValues(Map<Entity, List<SubEntity>> entityMap, Function<SubEntity, String> function, ColumnStats<Entity, SubEntity> columnStatistic) {
        ColumnValues cloneValues = new ColumnValues();
        List<SubEntity> collect = entityMap.values().stream().flatMap(Collection::stream).collect(toList());
        cloneValues.setTotalNumber(getTotalNumberBase(collect, function));
        cloneValues.setTotalDistinctNumber(getTotalDistinctNumber(collect, function));
        if (columnStatistic.isLimitedValues()) {
            cloneValues.setHistogram(getHistogram(entityMap, function));
        }
        cloneValues.setMultiplicity(getCardinalityPerRow(entityMap, function));
        return cloneValues;
    }


    private Range<Integer> getCardinalityPerRow(Map<Entity, List<SubEntity>> map, Function<SubEntity, String> function) {
        Map<String, Integer> cardinality = new HashMap<>();
        map.forEach((publication, list) ->
            cardinality.put(publication.getZdbID(), list.stream().filter(entity -> function.apply(entity) != null).toList().size()));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    protected <ID> long getTotalNumberBase(List<SubEntity> list, Function<SubEntity, ID> function) {
        if (list == null)
            return 0;
        return (int) list.stream()
            .filter(o -> function.apply(o) != null)
            .count();
    }

    protected <ID> long getTotalDistinctNumber(List<SubEntity> map, Function<SubEntity, ID> function) {
        if (map == null)
            return 0;
        return map.stream()
            .filter(o -> function.apply(o) != null)
            .map(function)
            .distinct()
            .count();
    }

    protected static <T extends ZdbID, O> long getTotalDistinctNumberOnObject(Collection<List<T>> map, Function<T, O> function) {
        return map.stream()
            .flatMap(Collection::stream)
            .map(function)
            .distinct()
            .count();
    }

    private Map<String, Integer> getHistogram(Map<Entity, List<SubEntity>> alleleMap, Function<SubEntity, String> function) {
        Map<String, List<String>> histogramRaw = alleleMap.values().stream()
            .flatMap(Collection::stream)
            .map(function)
            .toList()
            .stream()
            .collect(groupingBy(o -> {
                if (o == null)
                    return "<empty>";
                return o;
            }));
        return getValueSortedMap(histogramRaw);
    }

    protected Map<String, Integer> getHistogramOnUberEntity(Set<Entity> entitySet, Function<Entity, String> function) {
        Map<String, List<String>> histogramRaw = entitySet.stream()
            .map(function)
            .toList()
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

    protected ColumnValues populateSubEntityColumnStat(Map<Entity, List<SubEntity>> entitytMap,
                                                       ColumnStats<Entity, SubEntity> columnStats,
                                                       Map<Entity, List<SubEntity>> unfilteredEntitytMap) {
        ColumnValues colValues = getColumnValues(entitytMap, columnStats.getSingleValueSubEntityFunction(), columnStats);
        // no histogram available then no need to pick the complete list from unfiltered list
        if (!columnStats.isLimitedValues()) {
            return colValues;
        }
        ColumnValues colValuesUnfiltered = getColumnValues(unfilteredEntitytMap, columnStats.getSingleValueSubEntityFunction(), columnStats);
        Map<String, Integer> histogramFiltered = colValues.getHistogram();
        Map<String, Integer> histogramUnfiltered = colValuesUnfiltered.getHistogram();
        if (histogramUnfiltered == null) {
            return null;
        }
        populateFilteredCountsOnUnfilteredHistogram(histogramUnfiltered, histogramFiltered);
        colValues.setHistogram(histogramUnfiltered);
        return colValues;
    }

    protected void populateFilteredCountsOnUnfilteredHistogram(Map<String, Integer> histogramUnfiltered, Map<String, Integer> histogramFiltered) {
        histogramUnfiltered.forEach((key, count) ->
        {
            Integer filteredCount = histogramFiltered.get(key);
            if (filteredCount == null) {
                filteredCount = 0;
            }
            histogramUnfiltered.put(key, filteredCount);
        });
    }

    protected ColumnValues populateColumnStat(Set<Entity> entitySet, ColumnStats<Entity, Transcript> columnsStats, Set<Entity> unfilteredEntitySet) {
        ColumnValues columnValValues = new ColumnValues();
        columnValValues.setTotalNumber(entitySet.size());
        columnValValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(new ArrayList<>(entitySet)), columnsStats.getSingleValueEntityFunction()));
        if (columnsStats.isLimitedValues()) {
            Map<String, Integer> filteredHistogram = getHistogramOnUberEntity(entitySet, columnsStats.getSingleValueEntityFunction());
            Map<String, Integer> unfilteredHistogram = getHistogramOnUberEntity(unfilteredEntitySet, columnsStats.getSingleValueEntityFunction());
            populateFilteredCountsOnUnfilteredHistogram(unfilteredHistogram, filteredHistogram);
            columnValValues.setHistogram(unfilteredHistogram);
        }
        return columnValValues;
    }


}
