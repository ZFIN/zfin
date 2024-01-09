package org.zfin.stats;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbID;

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

    private <ID> Long getTotalDistinctNumber(Collection<List<SubEntity>> map, Function<SubEntity, List<ID>> function) {
        return map.stream()
            .flatMap(Collection::stream)
            .map(function)
            .flatMap(Collection::stream)
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
        ColumnValues columnValuesFiltered;
        if (columnStats.isMultiValued()) {
            columnValuesFiltered = getColumnValuesMultiValued(entitytMap, columnStats.getMultiValueSubEntityFunction());
        } else {
            columnValuesFiltered = getColumnValues(entitytMap, columnStats.getSingleValueSubEntityFunction(), columnStats);
        }
        // no histogram available then no need to pick the complete list from unfiltered list
        if (!columnStats.isLimitedValues()) {
            return columnValuesFiltered;
        }

        ColumnValues colValuesUnfiltered;
        if (columnStats.isMultiValued()) {
            colValuesUnfiltered = getColumnValuesMultiValued(unfilteredEntitytMap, columnStats.getMultiValueSubEntityFunction());
        } else {
            colValuesUnfiltered = getColumnValues(unfilteredEntitytMap, columnStats.getSingleValueSubEntityFunction(), columnStats);
        }
        Map<String, Integer> histogramFiltered = columnValuesFiltered.getHistogram();
        Map<String, Integer> histogramUnfiltered = colValuesUnfiltered.getHistogram();
        if (histogramUnfiltered == null) {
            return null;
        }
        populateFilteredCountsOnUnfilteredHistogram(histogramUnfiltered, histogramFiltered);
        columnValuesFiltered.setHistogram(histogramUnfiltered);
        return columnValuesFiltered;
    }

    private ColumnValues getColumnValuesMultiValued(Map<Entity, List<SubEntity>> publicationMap, Function<SubEntity, List<String>> function) {
        ColumnValues assayValues = new ColumnValues();
        assayValues.setTotalNumber(getTotalNumberMultiValued(publicationMap.values(), function));
        assayValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.values(), function));
        assayValues.setHistogram(getHistogramMultiValuedOnUniqueRow(publicationMap.values(), function));
        assayValues.setCardinality(getCardinalityPerUniqueRow(publicationMap.values(), function));
        assayValues.setMultiplicity(getCardinalityPerUniqueRow(publicationMap.values(), function));
        assayValues.setUberHistogram(getHistogramMultiValued(publicationMap, function));
        return assayValues;
    }

    private Map<String, Integer> getHistogramMultiValued(Map<Entity, List<SubEntity>> alleleMap, Function<SubEntity, List<String>> function) {
        Map<String, List<String>> histogramRaw = alleleMap.values().stream()
            .flatMap(Collection::stream)
            .map(function)
            .flatMap(Collection::stream)
            .toList()
            .stream()
            .collect(groupingBy(o -> {
                if (o == null)
                    return "";
                return o;
            }));
        return getValueSortedMap(histogramRaw);
    }


    private Map<String, Integer> getHistogramMultiValuedOnUniqueRow(Collection<List<SubEntity>> collection, Function<SubEntity, List<String>> function) {
        Map<String, List<String>> histogramRaw = collection.stream()
            .flatMap(Collection::stream)
            .map(function)
            .flatMap(Collection::stream)
            .toList()
            .stream()
            .collect(groupingBy(o -> {
                if (o == null)
                    return "<empty>";
                return o;
            }));
        return getValueSortedMap(histogramRaw);
    }

    private int getTotalNumberMultiValued(Collection<List<SubEntity>> map, Function<SubEntity, List<String>> function) {
        return map.stream()
            .flatMap(Collection::stream)
            .map(function)
            .mapToInt(List::size).sum();
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

    protected ColumnValues populateColumnStat(Set<Entity> entitySet, ColumnStats<Entity, SubEntity> columnsStats, Set<Entity> unfilteredEntitySet) {
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

    protected void addSubEntityColumnStatsToStatRow(List<ColumnStats<Entity, SubEntity>> subEntityColStats, Map<Entity, List<SubEntity>> filteredMap, Map<Entity, List<SubEntity>> unfilteredMap, StatisticRow<Entity, SubEntity> row) {
        subEntityColStats.forEach(columnStats -> row.put(columnStats, populateSubEntityColumnStat(filteredMap, columnStats, unfilteredMap)));
    }

    protected StatisticRow<Entity, SubEntity> addEntityColumnStatsToStatRow(List<ColumnStats<Entity, SubEntity>> entityColumnStats, Set<Entity> filteredSet, Set<Entity> unfilteredSet) {
        StatisticRow<Entity, SubEntity> row = new StatisticRow<>();
        entityColumnStats.forEach(columnStats -> row.put(columnStats, populateColumnStat(filteredSet, columnStats, unfilteredSet)));
        return row;
    }

    protected List<StatisticRow<Entity, SubEntity>> getStatisticResultRows(Map<Entity, List<SubEntity>> geneMap, StatisticRow<Entity, SubEntity> row) {
        // remove empty marker sets
        Map<Entity, Integer> sortedMap = getSortedEntityMap(geneMap);

        List<StatisticRow<Entity, SubEntity>> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow<Entity, SubEntity> statRow = new StatisticRow<>();
            row.getColumns().values().stream().filter(column -> column.getColumnDefinition().isSuperEntity())
                .forEach(column -> {
                    ColumnValues colValues = new ColumnValues();
                    colValues.setValue(column.getColumnDefinition().getSingleValueEntityFunction().apply(key));
                    statRow.put(column.getColumnDefinition(), colValues);
                });

            row.getColumns().values().stream().filter(column -> !column.getColumnDefinition().isSuperEntity())
                .forEach(columnStats -> {
                    ColumnValues columnValues = new ColumnValues();
                    if (columnStats.getColumnDefinition().isMultiValued()) {
                        columnValues.setTotalNumber(getTotalNumberMultiValuedObject(geneMap.get(key), columnStats.getColumnDefinition().getMultiValueSubEntityFunction()));
                        columnValues.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(geneMap.get(key), columnStats.getColumnDefinition().getMultiValueSubEntityFunction()));
                        columnValues.setCardinality(getCardinalityPerUniqueRow(List.of(geneMap.get(key)), columnStats.getColumnDefinition().getMultiValueSubEntityFunction()));

                    } else {
                        columnValues.setTotalNumber(getTotalNumberBase(geneMap.get(key), columnStats.getColumnDefinition().getSingleValueSubEntityFunction()));
                        columnValues.setTotalDistinctNumber(getTotalDistinctNumber(geneMap.get(key), columnStats.getColumnDefinition().getSingleValueSubEntityFunction()));
                    }
                    statRow.put(columnStats.getColumnDefinition(), columnValues);
                });

            rows.add(statRow);
        });
        return rows;
    }

    private <ID> Range<Integer> getCardinalityPerUniqueRow(Collection<List<SubEntity>> map, Function<SubEntity, List<ID>> function) {
        Map<String, Integer> cardinality = map.stream()
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .collect(toMap(ZdbID::getZdbID,
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


    private <ID> int getTotalDistinctNumberPerUberEntity(List<SubEntity> alleles, Function<SubEntity, List<ID>> function) {
        return alleles.stream()
            .map(function)
            .flatMap(Collection::stream)
            .collect(toSet())
            .size();
    }


    private int getTotalNumberMultiValuedObject(List<SubEntity> map, Function<SubEntity, List<String>> function) {
        return map.stream()
            .map(function)
            .filter(Objects::nonNull)
            .mapToInt(List::size).sum();
    }


    protected JsonResultResponse<StatisticRow<Entity, SubEntity>> getJsonResultResponse(Pagination pagination, StatisticRow<Entity, SubEntity> row, List<StatisticRow<Entity, SubEntity>> resultRows) {
        JsonResultResponse<StatisticRow<Entity, SubEntity>> response = new JsonResultResponse<>();
        response.setResults(resultRows);
        response.setTotal(resultRows.size());
        response.setResults(resultRows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }


}
