package org.zfin.stats;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;
import org.zfin.antibody.Antibody;
import org.zfin.feature.Feature;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.framework.api.*;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationDbXref;
import org.zfin.zebrashare.ZebrashareSubmissionMetadata;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static org.zfin.repository.RepositoryFactory.*;

public class StatisticPublicationService {

    public JsonResultResponse<StatisticRow> getAllAttributionStats(Pagination pagination) {
        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        return response;
    }

    public JsonResultResponse<StatisticRow> getAllPublicationGenes(Pagination pagination) {
        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        return response;
    }

    public JsonResultResponse<StatisticRow> getAllZebrashareStats(Pagination pagination) {

        Map<Publication, List<ZebrashareSubmissionMetadata>> publicationMap = getZebrashareRepository().getAllZebrashareFromPublication().stream()
            .collect(groupingBy(ZebrashareSubmissionMetadata::getPublication));

        // filter records
/*
        publicationMap.keySet().forEach(key -> {
            List<Feature> list = publicationMap.get(key);
            if (list != null) {
                FilterService<Feature> filterService = new FilterService<>(new AntibodyFiltering());
                List<Feature> antibodies = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, antibodies);
            }
        });
*/
        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap;
        // default sorting: number of entities
        integerMap = publicationMap.entrySet().stream()
            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<Publication> unfilteredPubList = new ArrayList<>(publicationMap.keySet());

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

        ColumnStats publicationTypeStat = getColumnStatsPubType(publicationMap, row, unfilteredPubList);

        ColumnValues columnValValues = new ColumnValues();
        columnValValues.setTotalNumber(publicationMap.size());
        List<Publication> arrayList = new ArrayList<>();
        arrayList.addAll(publicationMap.keySet());
        columnValValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(arrayList), Publication::getShortAuthorList));
        row.put(publicationNameStat, columnValValues);

        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(key.getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(key.getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

            ColumnValues colPubTypeValues = new ColumnValues();
            colPubTypeValues.setValue(key.getType().getDisplay());
            statRow.put(publicationTypeStat, colPubTypeValues);

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }


    public JsonResultResponse<StatisticRow> getDataSetsStats(Pagination pagination) {
        Map<Publication, List<PublicationDbXref>> publicationMap = getPublicationRepository().getAllDataSetsPublication();

        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap;
        // default sorting: number of entities
        integerMap = publicationMap.entrySet().stream()
            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<Publication> unfilteredPubList = new ArrayList<>(publicationMap.keySet());

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

        Map<ColumnStats, Function<PublicationDbXref, String>> columns = new LinkedHashMap<>();
        columns.put(
            new ColumnStats("Accession ID", false, true, false, false),
            PublicationDbXref::getAccessionNumber);
        columns.put(
            new ColumnStats("Type", false, false, false, false),
            publicationDbXref -> publicationDbXref.getReferenceDatabase().getForeignDB().getDbName().name());

        // put all columns into a statistic row
        addColumnsToRows(publicationMap, row, columns);

/*
        Map<ColumnStats, Function<PublicationDbXref, List<String>>> columnsMulti = new LinkedHashMap<>();
        columnsMulti.put(
            new ColumnStats("Type", false, false, true, true),
            publicationDbXref -> publicationDbXref.getReferenceDatabase().getForeignDB().getDbName());
        columnsMulti.put(
            new ColumnStats("Antigen Genes", false, false, true, false),
            antibody -> antibody.getAntigenGenes().stream().map(Marker::getAbbreviation).collect(toList()));

        addColumnsToRowsList(publicationMap, row, columnsMulti);
*/


        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(key.getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(key.getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

            columns.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberBase(publicationMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(key), function));
                statRow.put(columnStats, columnValues);
            });

/*
            columnsMulti.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberMultiValuedObject(publicationMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(publicationMap.get(key), function));
                columnValues.setCardinality(getCardinalityPerUniqueRow(List.of(publicationMap.get(key)), function));
                statRow.put(columnStats, columnValues);
            });
*/

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    public JsonResultResponse<StatisticRow> getAllPublicationStrs(Pagination pagination) {
        Map<Publication, List<STRTargetRow>> originalMap = getPublicationRepository().getAllAttributedSTRs(pagination);

        // Keep unfiltered data for histogram (deep copy before any filtering)
        Map<Publication, List<STRTargetRow>> unfilteredPublicationMap = new HashMap<>();
        originalMap.forEach((pub, strs) -> unfilteredPublicationMap.put(pub, new ArrayList<>(strs)));
        // remove empty publications from unfiltered map
        unfilteredPublicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));

        Map<Publication, List<STRTargetRow>> publicationMap = new HashMap<>();
        originalMap.keySet().forEach(key -> {
            List<STRTargetRow> list = originalMap.get(key);
            if (list != null) {
                FilterService<STRTargetRow> filterService = new FilterService<>(new STRTargetRowFiltering());
                List<STRTargetRow> strRow = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, strRow);
            }
        });
        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));

        HashMap<Publication, Integer> integerMap;
        // default sorting: number of entities
        integerMap = publicationMap.entrySet().stream()
            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

        Map<ColumnStats, Function<STRTargetRow, String>> columns = new LinkedHashMap<>();
        columns.put(
            new ColumnStats("Target Gene", false, false, false, false),
            strTargetRow -> strTargetRow.getTarget().getAbbreviation());
        columns.put(
            new ColumnStats("Reagent", false, false, false, false),
            strTargetRow -> strTargetRow.getStr().getAbbreviation());
        columns.put(
            new ColumnStats("Reagent Type", false, false, false, true),
            strTargetRow -> strTargetRow.getStr().getType().toString());

        // put all columns into a statistic row using unfiltered data for histogram
        addColumnsToRows(unfilteredPublicationMap, row, columns);

        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(key.getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(key.getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

            columns.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberBase(publicationMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(key), function));
                statRow.put(columnStats, columnValues);
            });

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    public JsonResultResponse<StatisticRow> getAllPublicationMutation(Pagination pagination) {

        //Map<Publication, List<Feature>> publicationMap = new HashMap<>(getPublicationRepository().getAllFeatureFromPublication());
        Map<Publication, List<Feature>> publicationMap = new HashMap<>();

        // filter records

/*
        publicationMap.keySet().forEach(key -> {
            List<Feature> list = publicationMap.get(key);
            if (list != null) {
                FilterService<Feature> filterService = new FilterService<>(new AntibodyFiltering());
                List<Feature> antibodies = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, antibodies);
            }
        });
*/
        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap = null;
        // default sorting: number of entities
        if (pagination.hasSortingValue()) {
/*
            switch (pagination.getSortFilter()) {
                case ASSAY:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getDistinctAssayNames);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
                case ANTIGEN_GENE:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getAntigenGenes);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
                case ANTIBODY_NAME:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
            }
*/
        } else {
            integerMap = publicationMap.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        }
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = new ColumnStats("Publication", true, false, false, false);
        ColumnValues columnValues = getColumnValuesUberEntity(publicationMap);
        row.put(publicationStat, columnValues);

        ColumnStats publicationNameStat = new ColumnStats("Pub Short Author", true, false, true, false);
        ColumnValues columnValValues = new ColumnValues();
        columnValValues.setTotalNumber(publicationMap.size());
        List<Publication> arrayList = new ArrayList<>();
        arrayList.addAll(publicationMap.keySet());
        columnValValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(arrayList), Publication::getShortAuthorList));
        row.put(publicationNameStat, columnValValues);


        ColumnStats alleleStat = new ColumnStats("Allele", false, true, false, false);
        ColumnValues antibodyValues = getColumnValuesRowEntity(publicationMap);
        row.put(alleleStat, antibodyValues);

        ColumnStats antibodyClonalTypeStat = new ColumnStats("Feature Type", false, false, false, true);
        ColumnValues cloneValues = getColumnValues(publicationMap, (feature -> feature.getType().name()));
        row.put(antibodyClonalTypeStat, cloneValues);
        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.entrySet().forEach(pubEntry -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(pubEntry.getKey().getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(pubEntry.getKey().getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

            ColumnValues colValueAllele = new ColumnValues();
            colValueAllele.setTotalNumber(pubEntry.getValue());
            statRow.put(alleleStat, colValueAllele);

            ColumnValues colValueFeatureType = new ColumnValues();
            colValueFeatureType.setTotalNumber(getTotalNumberBase(publicationMap.get(pubEntry.getKey()), (feature -> feature.getType().name())));
            colValueFeatureType.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(pubEntry.getKey()), (feature -> feature.getType().name())));
            statRow.put(antibodyClonalTypeStat, colValueFeatureType);

            rows.add(statRow);
        });


        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    public JsonResultResponse<StatisticRow> getAllFishStats(Pagination pagination) {

        //Map<Publication, List<Fish>> publicationMap = new HashMap<>(getPublicationRepository().getAllFishWithPublication());
        Map<Publication, List<Fish>> publicationMap = new HashMap<>();

        // filter records

        publicationMap.keySet().forEach(key -> {
            List<Fish> list = publicationMap.get(key);
            if (list != null) {
                FilterService<Fish> filterService = new FilterService<>(new FishFiltering());
                List<Fish> fishList = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, fishList);
            }
        });

        List<Publication> unfilteredPubList = new ArrayList<>(publicationMap.keySet());
        // filter on Publication
        filterOnPublication(pagination, publicationMap);

        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap = null;
        // default sorting: number of entities
        if (pagination.hasSortingValue()) {
/*
            switch (pagination.getSortFilter()) {
                case ASSAY:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getDistinctAssayNames);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
                case ANTIGEN_GENE:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getAntigenGenes);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
                case ANTIBODY_NAME:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
            }
*/
        } else {
            integerMap = publicationMap.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        }
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

        ColumnStats publicationTypeStat = getColumnStatsPubType(publicationMap, row, unfilteredPubList);

        ColumnStats fishStat = new ColumnStats("Fish", false, true, false, false);
        ColumnValues antibodyValues = getColumnValuesRowEntity(publicationMap);
        row.put(fishStat, antibodyValues);

        ColumnStats fishTypeStat = new ColumnStats("Wild Type", false, false, false, true);
        ColumnValues cloneValues = getColumnValues(publicationMap, (fish -> String.valueOf(fish.isWildtype())));
        row.put(fishTypeStat, cloneValues);
        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(key.getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(key.getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

            ColumnValues colPubTypeValues = new ColumnValues();
            colPubTypeValues.setValue(key.getType().getDisplay());
            statRow.put(publicationTypeStat, colPubTypeValues);

            ColumnValues colValueAllele = new ColumnValues();
            colValueAllele.setTotalNumber(value);
            statRow.put(fishStat, colValueAllele);

            ColumnValues colValueFeatureType = new ColumnValues();
            colValueFeatureType.setTotalNumber(getTotalNumberBase(publicationMap.get(key), (fish -> String.valueOf(fish.isWildtype()))));
            colValueFeatureType.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(key), (fish -> String.valueOf(fish.isWildtype()))));
            statRow.put(fishTypeStat, colValueFeatureType);

            rows.add(statRow);
        });


        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    private <Entity extends ZdbID> ColumnStats getColumnStatsPubType(Map<Publication, List<Entity>> publicationMap, StatisticRow row, List<Publication> unfilteredPubListForHistogramValues) {
        ColumnStats publicationTypeStat = new ColumnStats("Pub Type", true, false, true, true);
        ColumnValues columnValTypeValues = new ColumnValues();
        columnValTypeValues.setTotalNumber(publicationMap.size());
        List<Publication> pubList = new ArrayList<>();
        if (unfilteredPubListForHistogramValues == null)
            pubList.addAll(publicationMap.keySet());
        else
            pubList = unfilteredPubListForHistogramValues;
        columnValTypeValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(pubList), (pub -> pub.getType().getDisplay())));
        columnValTypeValues.setHistogram(getHistogramOnUber(pubList, (publication) -> publication.getType().getDisplay()));
        row.put(publicationTypeStat, columnValTypeValues);
        return publicationTypeStat;
    }

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

    private <T extends ZdbID> ColumnStats getColumnStatsPublication(Map<Publication, List<T>> publicationMap, StatisticRow row) {
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

    public JsonResultResponse<StatisticRow> getAllPublicationAntibodies(Pagination pagination) {

/*
        String subEntityFilter = pagination.getFieldFilterValueMap().get(FieldFilter.SUB_ENTITY);
        if (subEntityFilter != null && !subEntityFilter.isEmpty()) {
            Map<String, List<Allele>> filteredAlleleMap1;
            String[] subEntitySplit = subEntityFilter.split(";");
            String subEntityName = subEntitySplit[0];
            String subEntityFilterValue = subEntitySplit[1];
            ColumnStats<Allele, ?> columnStats = getSubEntityRowColumn().stream()
                    .filter(col -> col.getName().equals(subEntityName)).findFirst().get();

            filteredAlleleMap1 = filteredAlleleMap.entrySet().stream()
                    .filter(entry ->
                            entry.getValue().stream()
                                    .anyMatch(antibodyStat -> columnStats.getSingleValuefunction().apply(antibodyStat).equalsIgnoreCase(subEntityFilterValue))
                    )
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            filteredAlleleMap = filteredAlleleMap1;
        }
*/

        // filter by sub entity cardinality
/*
        String subEntityFilterCardinality = pagination.getFieldFilterValueMap().get(FieldFilter.SUB_ENTITY_CARDINALITY);
        if (subEntityFilterCardinality != null && !subEntityFilterCardinality.isEmpty()) {
            String[] subEntitySplit = subEntityFilterCardinality.split(";");
            String subEntityName = subEntitySplit[0];
            String subEntityFilterValue = subEntitySplit[1];
            int cardinality = Integer.parseInt(subEntityFilterValue);
            ColumnStats<Allele, ?> columnStats = getSubEntityRowColumn().stream()
                    .filter(col -> col.getName().equals(subEntityName)).findFirst().get();

            Map<String, List<Allele>> filteredAlleleMap1 = new LinkedHashMap<>();
            for (Map.Entry<String, List<Allele>> entry : filteredAlleleMap.entrySet()) {
                String gene = entry.getKey();
                List<Allele> alleles = entry.getValue().stream()
                        .filter(antibodyStat -> columnStats.getMultiValueFunction().apply(antibodyStat).size() == cardinality)
                        .collect(toList());
                if (CollectionUtils.isNotEmpty(alleles)) {
                    filteredAlleleMap1.put(gene, alleles);
                }

            }
            filteredAlleleMap = filteredAlleleMap1;
        }
*/

        Map<Publication, List<Antibody>> originalMap = getAntibodyRepository().getAntibodiesFromAllPublications();

        // Keep unfiltered data for histogram (deep copy before any filtering)
        Map<Publication, List<Antibody>> unfilteredPublicationMap = new HashMap<>();
        originalMap.forEach((pub, antibodies) -> unfilteredPublicationMap.put(pub, new ArrayList<>(antibodies)));

        Map<Publication, List<Antibody>> publicationMap = new HashMap<>();
        originalMap.forEach((pub, antibodies) -> publicationMap.put(pub, new ArrayList<>(antibodies)));

        List<Publication> unfilteredPubList = new ArrayList<>(publicationMap.keySet());
        // filter on Publication
        filterOnPublication(pagination, publicationMap);

        // filter records
        publicationMap.keySet().forEach(key -> {
            List<Antibody> list = publicationMap.get(key);
            if (list != null) {
                FilterService<Antibody> filterService = new FilterService<>(new AntibodyFiltering());
                List<Antibody> antibodies = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, antibodies);
            }
        });
        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap = null;
        // default sorting: number of entities
        if (pagination.hasSortingValue()) {
            switch (pagination.getSortFilter()) {
                case ASSAY:
                    integerMap = publicationMap.entrySet().stream()
                        .collect(HashMap::new, (map, entry) -> {
                            Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getDistinctAssayNames);
                            map.put(entry.getKey(), (Integer) range.getMaximum());
                        }, HashMap::putAll);
                    break;
                case ANTIGEN_GENE:
                    integerMap = publicationMap.entrySet().stream()
                        .collect(HashMap::new, (map, entry) -> {
                            Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getAntigenGenes);
                            map.put(entry.getKey(), (Integer) range.getMaximum());
                        }, HashMap::putAll);
                    break;
                case ANTIBODY_NAME:
                    integerMap = publicationMap.entrySet().stream()
                        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
            }
        } else {
            integerMap = publicationMap.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        }
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

        //ColumnStats publicationTypeStat = getColumnStatsPubType(publicationMap, row, unfilteredPubList);

/*
        ColumnStats publicationNameStat = new ColumnStats("Pub Short Author", true, false, true, false);
        ColumnValues columnValValues = new ColumnValues();
        columnValValues.setTotalNumber(publicationMap.size());
        List<Publication> arrayList = new ArrayList<>();
        arrayList.addAll(publicationMap.keySet());
        columnValValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(arrayList), Publication::getShortAuthorList));
        row.put(publicationNameStat, columnValValues);
*/

        Map<ColumnStats, Function<Antibody, String>> columns = new LinkedHashMap<>();
        columns.put(
            new ColumnStats("Antibody Name", false, true, false, false),
            Antibody::getName);
        columns.put(
            new ColumnStats("Clonal Type", false, false, false, true),
            Antibody::getClonalType);
        columns.put(
            new ColumnStats("Isotype", false, false, false, true),
            Antibody::getHeavyChainIsotype);
        columns.put(
            new ColumnStats("Host Organism", false, false, false, true),
            Antibody::getHostSpecies);
        // put all columns into a statistic row using unfiltered data for histogram
        addColumnsToRows(unfilteredPublicationMap, row, columns);

        Map<ColumnStats, Function<Antibody, List<String>>> columnsMulti = new LinkedHashMap<>();
        columnsMulti.put(
            new ColumnStats("Assay", false, false, true, true),
            Antibody::getDistinctAssayNames);
        columnsMulti.put(
            new ColumnStats("Antigen Genes", false, false, true, false),
            antibody -> antibody.getAntigenGenes().stream().map(Marker::getAbbreviation).collect(toList()));

        addColumnsToRowsList(unfilteredPublicationMap, row, columnsMulti);
/*

        ColumnStats assay = new ColumnStats("Assay", false, false, true, true);
        ColumnValues assayValues = getColumnValuesMultiValued(publicationMap, Antibody::getDistinctAssayNames);
        row.put(assay, assayValues);

        ColumnStats geneStat = new ColumnStats("Antigen Genes", false, false, true, false);
        ColumnValues geneValues = new ColumnValues();
        geneValues.setTotalNumber(getTotalNumberMultiValuedObject(publicationMap.values(), Antibody::getAntigenGenes));
        geneValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.values(), Antibody::getAntigenGenes));
        //geneValues.setHistogram(getHistogramMultiValuedOnUniqueRow(publicationMap.values(), Antibody::getAntigenGenes));
        geneValues.setCardinality(getCardinalityPerUniqueRow(publicationMap.values(), Antibody::getAntigenGenes));
        //geneValues.setUberHistogram(getHistogramMultiValued(publicationMap, Antibody::getAntigenGenes));

        row.put(geneStat, geneValues);
*/

/*
            getSubEntityRowColumn().forEach(columnStats -> {
                ColumnValues columnValues1 = new ColumnValues();
                if (columnStats.isMultiValued()) {
                    columnValues1.setTotalNumber(getTotalNumberPerUberEntity(alleles, columnStats.getMultiValueFunction()));
                    columnValues1.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(alleles, columnStats.getMultiValueFunction()));
                    columnValues1.setCardinality(getCardinalityPerUberEntity(alleles, columnStats.getMultiValueFunction()));
                    if (alleles.size() == 1) {
                        columnValues1.setValue(getMultiValues(alleles, columnStats.getMultiValueFunction()));
                    }
                } else {
                    // calculate the histogram of possible values
                    if (columnStats.getSingleValuefunction() != null) {
                        Map<String, List<String>> unSortedHistogram = alleles.stream()
                                .map(columnStats.getSingleValuefunction())
                                .collect(toList())
                                .stream()
                                .collect(groupingBy(o -> o));
                        Map<String, Integer> sortedHistogram = getValueSortedMap(unSortedHistogram);
                        columnValues1.setHistogram(sortedHistogram);
                        columnValues1.setTotalDistinctNumber(sortedHistogram.size());
                        columnValues1.setTotalNumber(sortedHistogram.size());
                    }
                }
                row.put(columnStats, columnValues1);

            });
*/

        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(key.getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(key.getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

/*
            ColumnValues colPubTypeValues = new ColumnValues();
            colPubTypeValues.setValue(pubEntry.getKey().getType().getDisplay());
            statRow.put(publicationTypeStat, colPubTypeValues);

*/


            columns.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberBase(publicationMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(key), function));
                statRow.put(columnStats, columnValues);
            });

            columnsMulti.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberMultiValuedObject(publicationMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(publicationMap.get(key), function));
                columnValues.setCardinality(getCardinalityPerUniqueRow(List.of(publicationMap.get(key)), function));
                statRow.put(columnStats, columnValues);
            });

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    private long getTotalNumberMultiValuedObject1(List<List<Antibody>> lists, Function<Antibody, List<String>> function) {
        return 0;
    }

    public JsonResultResponse<StatisticRow> getAllPublicationExpression(Pagination pagination) {

        Map<Publication, List<ExpressionTableRow>> publicationMap = new HashMap<>(getPublicationPageRepository().getAllPublicationExpression(pagination));


        List<Publication> unfilteredPubList = new ArrayList<>(publicationMap.keySet());
        // filter on Publication
        filterOnPublication(pagination, publicationMap);

        // filter records
        publicationMap.keySet().forEach(key -> {
            List<ExpressionTableRow> list = publicationMap.get(key);
            if (list != null) {
                FilterService<ExpressionTableRow> filterService = new FilterService<>(new ExpressionTableRowFiltering());
                List<ExpressionTableRow> expressionTableRows = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, expressionTableRows);
            }
        });
        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap = null;
        // default sorting: number of entities
        if (pagination.hasSortingValue()) {
            switch (pagination.getSortFilter()) {
/*
                case ASSAY:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getDistinctAssayNames);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
                case ANTIGEN_GENE:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getAntigenGenes);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
*/
                case ANTIBODY -> {
                    Function<ExpressionTableRow, String> function = expressionTableRow -> {
                        if (expressionTableRow.getAntibody() != null)
                            return expressionTableRow.getAntibody().getAbbreviation();
                        return null;
                    };
                    integerMap = getCardinalityPerRowPub(publicationMap, function);
                }
                case ANTIBODY_NAME -> integerMap = publicationMap.entrySet().stream()
                    .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
                case QUALIFIER -> {
                    Function<ExpressionTableRow, String> functionQualifier = expressionTableRow -> {
                        if (expressionTableRow.getQualifier() != null)
                            return expressionTableRow.getQualifier();
                        return null;
                    };
                    integerMap = getCardinalityPerRowPub(publicationMap, functionQualifier);
                }
            }
        } else {
            integerMap = publicationMap.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        }

        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

//        ColumnStats publicationTypeStat = getColumnStatsPubType(publicationMap, row, unfilteredPubList);

        Map<ColumnStats, Function<ExpressionTableRow, String>> columns = new LinkedHashMap<>();
        columns.put(
            new ColumnStats("Gene Symbol", false, false, false, false),
            expressionTableRow -> expressionTableRow.getGene().getAbbreviation());
        columns.put(new
                ColumnStats("Antibody", false, false, false, false),
            expressionTableRow ->
            {
                if (expressionTableRow.getAntibody() != null)
                    return expressionTableRow.getAntibody().getAbbreviation();
                return null;
            });
        columns.put(
            new ColumnStats("Fish", false, false, false, false),
            expressionTableRow ->
            {
                if (expressionTableRow.getFish() != null)
                    return expressionTableRow.getFish().getName();
                return null;
            });
        columns.put(
            new ColumnStats("Experiment", false, false, false, false),
            expressionTableRow ->
            {
                if (expressionTableRow.getExperiment() != null)
                    return expressionTableRow.getExperiment().getDisplayAllConditions();
                return null;
            });
        columns.put(
            new ColumnStats("Stage", false, false, false, true),
            expressionTableRow -> expressionTableRow.getStart().getAbbreviation() + ":" + expressionTableRow.getEnd().getAbbreviation());
        columns.put(
            new ColumnStats("Qualifier", false, false, false, true),

            ExpressionTableRow::getQualifier);
        columns.put(
            new ColumnStats("Anatomy", false, false, false, false),
            expressionTableRow ->
            {
                String anatomy = expressionTableRow.getSuperterm().getTermName();
                if (expressionTableRow.getSubterm() != null)
                    anatomy += expressionTableRow.getSubterm().getTermName();
                return anatomy;
            });
        columns.put(
            new ColumnStats("Assay", false, false, false, true),
            expressionTableRow -> expressionTableRow.getAssay().getName());
        columns.put(
            new ColumnStats("Figure", false, false, false, false),
            expressionTableRow -> expressionTableRow.getFigure().
                getLabel());

        // put all columns into a statistic row
        addColumnsToRows(publicationMap, row, columns);


        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.entrySet().
            forEach(pubEntry ->
            {
                StatisticRow statRow = new StatisticRow();

                ColumnValues colValues = new ColumnValues();
                colValues.setValue(pubEntry.getKey().getZdbID());
                statRow.put(publicationStat, colValues);

                ColumnValues colPubNameValues = new ColumnValues();
                colPubNameValues.setValue(pubEntry.getKey().getShortAuthorList());
                statRow.put(publicationNameStat, colPubNameValues);

                columns.forEach((columnStats, function) -> {
                    ColumnValues columnValues = new ColumnValues();
                    columnValues.setTotalNumber(getTotalNumberBase(publicationMap.get(pubEntry.getKey()), function));
                    columnValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(pubEntry.getKey()), function));
                    statRow.put(columnStats, columnValues);
                });
                rows.add(statRow);
            });

        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
                .

            skip(pagination.getStart())
                .

            limit(pagination.getLimit())
                .

            collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
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

    private <T extends ZdbID> ColumnValues getColumnValuesUberEntity(Map<Publication, List<T>> publicationMap) {
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


    private <T extends ZdbID> ColumnValues getColumnValues(Map<Publication, List<T>> publicationMap, Function<T, String> function) {
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

    private static <T extends ZdbID, ID extends Object> Range getCardinalityPerRow(Map<Publication, List<T>> map, Function<T, String> function) {
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

    private static int getTotalNumberBaseSTR(List<Marker> list, Function<Marker, Object> function) {
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


    private static <T extends ZdbID> Map<String, Integer> getHistogram(Map<Publication, List<T>> alleleMap, Function<T, String> function) {
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

    public JsonResultResponse<StatisticRow> getAllProbeStats(Pagination pagination) {

        pagination.setLimit(1000000);
        Map<Publication, List<Clone>> originalMap = getPublicationPageRepository().getAllProbes(pagination);

        // Keep unfiltered data for histogram (deep copy before any filtering)
        Map<Publication, List<Clone>> unfilteredPublicationMap = new HashMap<>();
        originalMap.forEach((pub, clones) -> unfilteredPublicationMap.put(pub, new ArrayList<>(clones)));

        Map<Publication, List<Clone>> publicationMap = new HashMap<>();
        originalMap.forEach((pub, clones) -> publicationMap.put(pub, new ArrayList<>(clones)));

        List<Publication> unfilteredPubList = new ArrayList<>(publicationMap.keySet());
        // filter on Publication
        filterOnPublication(pagination, publicationMap);

        // filter records
        publicationMap.keySet().forEach(key -> {
            List<Clone> list = publicationMap.get(key);
            if (list != null) {
                FilterService<Clone> filterService = new FilterService<>(new CloneFiltering());
                List<Clone> filteredClones = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, filteredClones);
            }
        });
        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap = null;
        // default sorting: number of entities
        if (pagination.hasSortingValue()) {
            switch (pagination.getSortFilter()) {
/*
                case ASSAY:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getDistinctAssayNames);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
                case ANTIGEN_GENE:
                    integerMap = publicationMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(List.of(entry.getValue()), Antibody::getAntigenGenes);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
*/
                case ANTIBODY -> {
                    Function<Clone, String> function = expressionTableRow -> {
                        if (expressionTableRow.getName() != null)
                            return expressionTableRow.getName();
                        return null;
                    };
                    integerMap = getCardinalityPerRowPub(publicationMap, function);
                }
                case ANTIBODY_NAME -> integerMap = publicationMap.entrySet().stream()
                    .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
            }
        } else {
            integerMap = publicationMap.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        }

        Map<Publication, Integer> sortedMap = integerMap.entrySet().
            stream().
            sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

//        ColumnStats publicationTypeStat = getColumnStatsPubType(publicationMap, row, unfilteredPubList);

        Map<ColumnStats, Function<Clone, String>> columns = new LinkedHashMap<>();
        columns.put(
            new ColumnStats("Probe", false, false, false, false),
            expressionTableRow -> expressionTableRow.getName());
        columns.put(
            new ColumnStats("Type", false, false, false, true),
            expressionTableRow -> expressionTableRow.getType().name());

        // put all columns into a statistic row using unfiltered data for histogram
        addColumnsToRows(unfilteredPublicationMap, row, columns);


        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(key.getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(key.getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

            columns.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberBase(publicationMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(key), function));
                statRow.put(columnStats, columnValues);
            });

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
                .

            skip(pagination.getStart())
                .

            limit(pagination.getLimit())
                .

            collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    public JsonResultResponse<StatisticRow> getAllDiseaseStats(Pagination pagination) {

        pagination.setLimit(1000000);
        List<DiseaseAnnotationModel> list1 = getPhenotypeRepository().getAllHumanDiseaseAnnotationModels();
        Map<Publication, List<DiseaseAnnotationModel>> originalMap = list1.stream()
            .collect(groupingBy(model -> model.getDiseaseAnnotation().getPublication()));

        // Keep unfiltered data for histogram (deep copy before any filtering)
        Map<Publication, List<DiseaseAnnotationModel>> unfilteredPublicationMap = new HashMap<>();
        originalMap.forEach((pub, models) -> unfilteredPublicationMap.put(pub, new ArrayList<>(models)));

        Map<Publication, List<DiseaseAnnotationModel>> publicationMap = new HashMap<>();
        originalMap.forEach((pub, models) -> publicationMap.put(pub, new ArrayList<>(models)));

        List<Publication> unfilteredPubList = new ArrayList<>(publicationMap.keySet());
        // filter on Publication
        //filterOnPublication(pagination, publicationMap);

        // filter records
        publicationMap.keySet().forEach(key -> {
            List<DiseaseAnnotationModel> list = publicationMap.get(key);
            if (list != null) {
                FilterService<DiseaseAnnotationModel> filterService = new FilterService<>(new DiseaseAnnotationModelFiltering());
                List<DiseaseAnnotationModel> models = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                publicationMap.put(key, models);
            }
        });
        // remove empty publications
        publicationMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));


        HashMap<Publication, Integer> integerMap = null;
        // default sorting: number of entities
        if (pagination.hasSortingValue()) {
            switch (pagination.getSortFilter()) {
                case EXPERIMENT -> integerMap = publicationMap.entrySet().stream()
                    .collect(HashMap::new, (map, entry) -> {
                        Range range = getCardinalityPerRow(publicationMap, model -> model.getFishExperiment().getExperiment().getDisplayAllConditions());
                        map.put(entry.getKey(), (Integer) range.getMaximum());
                    }, HashMap::putAll);
                case ANTIBODY_NAME -> integerMap = publicationMap.entrySet().stream()
                    .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
            }
        } else {
            integerMap = publicationMap.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        }

        Map<Publication, Integer> sortedMap = integerMap.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = getColumnStatsPublication(publicationMap, row);

        ColumnStats publicationNameStat = getColumnStatsPubAuthor(publicationMap, row);

        Map<ColumnStats, Function<DiseaseAnnotationModel, String>> columns = new LinkedHashMap<>();
        columns.put(
            new ColumnStats("Disease", false, false, false, false),
            diseaseModel -> diseaseModel.getDiseaseAnnotation().getDisease().getTermName());
        columns.put(
            new ColumnStats("Fish", false, false, false, true),
            diseaseModel -> {
                if (diseaseModel.getFishExperiment() != null) {
                    return diseaseModel.getFishExperiment().getFish().getName();
                } else {
                    return null;
                }
            });
        columns.put(
            new ColumnStats("Environment", false, false, false, true),
            diseaseModel -> {
                if (diseaseModel.getFishExperiment() != null) {
                    return diseaseModel.getFishExperiment().getExperiment().getDisplayAllConditions();
                } else {
                    return null;
                }
            });
        columns.put(
            new ColumnStats("Evidence", false, false, false, true),
            diseaseModel -> diseaseModel.getDiseaseAnnotation().getEvidenceCodeString());

        // put all columns into a statistic row using unfiltered data for histogram
        addColumnsToRows(unfilteredPublicationMap, row, columns);


        // create return result set
        List<StatisticRow> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(key.getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colPubNameValues = new ColumnValues();
            colPubNameValues.setValue(key.getShortAuthorList());
            statRow.put(publicationNameStat, colPubNameValues);

            columns.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberBase(publicationMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(key), function));
                statRow.put(columnStats, columnValues);
            });

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    private <T extends ZdbID> void addColumnsToRows(Map<Publication, List<T>> publicationMap, StatisticRow row, Map<ColumnStats, Function<T, String>> columns) {
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
