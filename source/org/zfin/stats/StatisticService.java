package org.zfin.stats;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;
import org.zfin.antibody.Antibody;
import org.zfin.framework.api.FilterService;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.PublicationFiltering;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.ZdbEntity;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationDbXref;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class StatisticService<E extends EntityZdbID> {


    private <T extends ZdbID> ColumnStats getColumnStatsPubAuthor(Map<Publication, List<T>> publicationMap, StatisticRow row) {
        ColumnStats publicationNameStat = new ColumnStats("Pub Short Author", true, false, true, false);
        ColumnValues columnValValues = new ColumnValues();
        columnValValues.setTotalNumber(publicationMap.size());
        List<Publication> arrayList = new ArrayList<>();
        arrayList.addAll(publicationMap.keySet());
        columnValValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(arrayList), Publication::getShortAuthorList));
        row.put(publicationNameStat, columnValValues);
        return publicationNameStat;
    }

    protected <T extends ZdbID> ColumnStats getColumnStatsEntity(Map<E, List<T>> publicationMap, StatisticRow row) {
        ColumnStats publicationStat = new ColumnStats("Publication", true, false, false, false);
        ColumnValues columnValues = getColumnValuesUberEntity(publicationMap);
        row.put(publicationStat, columnValues);
        return publicationStat;
    }

    private <Entity extends ZdbID> void filterOnPublication(Pagination pagination, Map<Publication, List<Entity>> publicationMap) {
        final List<Publication> filteredPubList = new ArrayList<>();
        pagination.getFieldFilterValueMap()
            .keySet().stream()
            .filter(fieldFilter -> fieldFilter.getName().startsWith("publication"))
            .forEach(fieldFilter -> {
                FilterService<Publication> filterService = new FilterService<>(new PublicationFiltering());
                List<Publication> pubList = new ArrayList<>(publicationMap.keySet());
                filteredPubList.addAll(filterService.filterAnnotations(pubList, pagination.getFieldFilterValueMap()));
            });
        if (!CollectionUtils.isEmpty(filteredPubList)) {
            publicationMap.keySet().removeIf(o -> !filteredPubList.contains(o));
        }
    }

    private <T extends ZdbID> ColumnValues getColumnValuesMultiValued(Map<Publication, List<T>> publicationMap, Function<T, List<String>> function) {
        ColumnValues assayValues = new ColumnValues();
        assayValues.setTotalNumber(getTotalNumberMultiValued(publicationMap.values(), function));
        assayValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.values(), function));
        assayValues.setHistogram(getHistogramMultiValuedOnUniqueRow(publicationMap.values(), function));
        assayValues.setCardinality(getCardinalityPerUniqueRow(publicationMap.values(), function));
        assayValues.setMultiplicity(getCardinalityPerUniqueRow(publicationMap.values(), function));
        assayValues.setUberHistogram(getHistogramMultiValued(publicationMap, function));
        return assayValues;
    }

    private <T extends ZdbID> ColumnValues getColumnValuesUberEntity(Map<E, List<T>> publicationMap) {
        ColumnValues columnValues = new ColumnValues();
        columnValues.setTotalNumber(publicationMap.size());
        return columnValues;
    }

    private <T extends ZdbID> ColumnValues getColumnValuesRowEntity(Map<Publication, List<T>> publicationMap) {
        ColumnValues antibodyValues = new ColumnValues();
        antibodyValues.setTotalNumber(getTotalNumber(publicationMap.values()));
        antibodyValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.values()));
        // multiplicity
        Map<String, List<String>> multipleSet = new HashMap<>();
        for (Map.Entry<Publication, List<T>> entry : publicationMap.entrySet()) {
            entry.getValue().forEach(allele1 -> {
                List<String> genes = multipleSet.get(allele1.getZdbID());
                if (genes == null)
                    genes = new ArrayList<>();
                genes.add(entry.getKey().getZdbID());
                multipleSet.put(allele1.getZdbID(), genes);
            });
        }
        antibodyValues.setMultiplicity(getCardinalityPerUniqueRow(publicationMap));
        return antibodyValues;
    }


    private <T extends ZdbID> ColumnValues getColumnValues(Map<E, List<T>> publicationMap, Function<T, String> function) {
        ColumnValues cloneValues = new ColumnValues();
        List<T> collect = publicationMap.values().stream().flatMap(Collection::stream).collect(toList());
        cloneValues.setTotalNumber(getTotalNumberBase(collect, function));
        cloneValues.setTotalDistinctNumber(getTotalDistinctNumber(collect, function));
        cloneValues.setMultiplicity(getCardinalityPerRow(publicationMap, function));
        cloneValues.setHistogram(getHistogram(publicationMap, function));
        return cloneValues;
    }

    public <T extends ZdbID> int getTotalNumber(Collection<List<T>> map) {
        return map.stream()
            .mapToInt(List::size).sum();
    }

    private static <ID> Range getCardinality(Map<String, List<Antibody>> alleleMap, Function<Antibody, List<ID>> function) {
        Map<String, Integer> cardinality = alleleMap.values().stream()
            .flatMap(Collection::stream)
            .collect(toMap(Antibody::getZdbID,
                o -> function.apply(o).size(),
                // if duplicates occur just take one.
                (a1, a2) -> a1));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private static <T extends ZdbID> Range getCardinalityPerUniqueRow(Map<Publication, List<T>> publicationMap) {
        Map<String, Integer> cardinality = publicationMap.entrySet().stream()
            .collect(toMap(entry -> entry.getKey().getZdbID(), entry -> entry.getValue().size()));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private static <T extends ZdbID, ID extends Object> Range getCardinalityPerUniqueRow(Collection<List<T>> map, Function<T, List<ID>> function) {
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

    private <T extends ZdbID, ID extends Object> Range getCardinalityPerRow(Map<E, List<T>> map, Function<T, String> function) {
        Map<String, Integer> cardinality = new HashMap<>();
        map.forEach((publication, list) ->
            cardinality.put(publication.getZdbID(), list.stream().filter(entity -> function.apply(entity) != null).toList().size()));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private static <T extends ZdbID, ID extends Object> HashMap<Publication, Integer> getCardinalityPerRowPub(Map<Publication, List<T>> map, Function<T, String> function) {
        HashMap<Publication, Integer> cardinality = new HashMap<>();
        map.forEach((publication, list) ->
            cardinality.put(publication, list.stream().filter(entity -> function.apply(entity) != null).toList().size()));
        return cardinality;
    }

    private static <T extends ZdbID, O extends Object> int getTotalNumberBase(List<T> list, Function<T, O> function) {
        if (list == null)
            return 0;
        return (int) list.stream()
            .filter(o -> function.apply(o) != null)
            .count();
    }

    private static <T extends ZdbID> int getTotalNumberMultiValued(Collection<List<T>> map, Function<T, List<String>> function) {
        return map.stream()
            .flatMap(Collection::stream)
            .map(function)
            .mapToInt(List::size).sum();
    }

    private static <T extends ZdbID> int getTotalNumberMultiValuedBase(List<T> map, Function<T, List<? extends Object>> function) {
        return map.stream()
            .map(function)
            .mapToInt(List::size).sum();
    }

    private static <T extends ZdbID> int getTotalNumberMultiValuedObject(List<T> map, Function<T, List<String>> function) {
        return map.stream()
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
    private static <T extends ZdbID> long getTotalDistinctNumber(Collection<List<T>> map) {
        return map.stream()
            .flatMap(Collection::stream)
            .map(ZdbID::getZdbID)
            .distinct()
            .count();
    }

    private static <T extends ZdbID, ID> Long getTotalDistinctNumber(Collection<List<T>> map, Function<T, List<ID>> function) {
        return map.stream()
            .flatMap(Collection::stream)
            .map(function)
            .flatMap(Collection::stream)
            .distinct()
            .count();
    }

    private static <T extends ZdbID, ID> long getTotalDistinctNumber(List<T> map, Function<T, ID> function) {
        if (map == null)
            return 0;
        return map.stream()
            .filter(o -> function.apply(o) != null)
            .map(function)
            .distinct()
            .count();
    }

    private static <T extends ZdbID, O> long getTotalDistinctNumberOnObject(Collection<List<T>> map, Function<T, O> function) {
        return map.stream()
            .flatMap(Collection::stream)
            .map(function)
            .distinct()
            .count();
    }

    private static <T extends ZdbID, ID> int getTotalDistinctNumberPerUberEntity(List<T> alleles, Function<T, List<ID>> function) {
        return alleles.stream()
            .map(function)
            .flatMap(Collection::stream)
            .collect(toSet())
            .size();
    }

    private Map<String, Integer> getHistogramOnUber(List<Publication> arrayTypeList, Function<Publication, String> function) {
        Map<String, List<String>> histogramRaw = arrayTypeList.stream()
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


    private <T extends ZdbID> Map<String, Integer> getHistogram(Map<E, List<T>> alleleMap, Function<T, String> function) {
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

    private static <T extends ZdbID> Map<String, Integer> getHistogramMultiValued(Map<Publication, List<T>> alleleMap, Function<T, List<String>> function) {
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

    private static <T extends ZdbID> Map<String, Integer> getHistogramMultiValuedOnUniqueRow(Collection<List<T>> collection, Function<T, List<String>> function) {
        Map<String, List<String>> histogramRaw = collection.stream()
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

    protected <T extends ZdbID> void addColumnsToRows(Map<E, List<T>> publicationMap, StatisticRow row, Map<ColumnStats, Function<T, String>> columns) {
        columns.forEach((columnStats, function) ->
        {
            ColumnValues colValues = getColumnValues(publicationMap, function);
            row.put(columnStats, colValues);
        });
    }

    private <T extends ZdbID> void addColumnsToRowsList(Map<Publication, List<T>> publicationMap, StatisticRow row, Map<ColumnStats, Function<T, List<String>>> columns) {
        columns.forEach((columnStats, function) ->
        {
            ColumnValues colValues = getColumnValuesMultiValued(publicationMap, function);
            row.put(columnStats, colValues);
        });
    }
}
