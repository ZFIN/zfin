package org.zfin.stats;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;
import org.zfin.antibody.Antibody;
import org.zfin.feature.Feature;
import org.zfin.framework.api.AntibodyFiltering;
import org.zfin.framework.api.FilterService;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.publication.Publication;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

public class StatisticPublicationService {

    public JsonResultResponse<StatisticRow> getAllPublicationStrs(Pagination pagination) {
        Map<Publication, List<SequenceTargetingReagent>> strMap = new HashMap<>(getAntibodyRepository().getSTRFromAllPublications());
        Map<Publication, List<Marker>> strTargetMap = new HashMap<>();
        strMap.forEach((key, value) -> {
            List<Marker> markerList = value.stream()
                    .map(SequenceTargetingReagent::getTargetGenes)
                    .flatMap(Collection::stream)
                    .collect(toList());
            strTargetMap.put(key, markerList);
        });

        // filter records

/*
        strMap.keySet().forEach(key -> {
            List<SequenceTargetingReagent> list = strMap.get(key);
            if (list != null) {
                FilterService<SequenceTargetingReagent> filterService = new FilterService<>(new AntibodyFiltering());
                List<SequenceTargetingReagent> antibodies = filterService.filterAnnotations(list, pagination.getFieldFilterValueMap());
                strMap.put(key, antibodies);
            }
        });
*/
        // remove empty publications
        strTargetMap.entrySet().removeIf(entry -> CollectionUtils.isEmpty(entry.getValue()));

        HashMap<Publication, Integer> integerMap = null;
        // default sorting: number of entities
        if (pagination.hasSortingValue()) {
            switch (pagination.getSortFilter()) {
                case TARGET_GENE:
                    integerMap = strTargetMap.entrySet().stream()
                            .collect(HashMap::new, (map, entry) -> {
                                Range range = getCardinalityPerUniqueRow(null);
                                map.put(entry.getKey(), (Integer) range.getMaximum());
                            }, HashMap::putAll);
                    break;
            }
        } else {
            integerMap = strMap.entrySet().stream()
                    .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        }
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
                stream().
                sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        StatisticRow row = new StatisticRow();

        ColumnStats publicationStat = new ColumnStats("Publication", true, false, false, false);
        ColumnValues columnValues = getColumnValuesUberEntity(strMap);
        row.put(publicationStat, columnValues);

        ColumnStats publicationNameStat = new ColumnStats("Pub Short Author", true, false, true, false);
        ColumnValues columnValValues = new ColumnValues();
        columnValValues.setTotalNumber(strMap.size());
        List<Publication> arrayList = new ArrayList<>();
        arrayList.addAll(strTargetMap.keySet());
        columnValValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(arrayList), Publication::getShortAuthorList));
        row.put(publicationNameStat, columnValValues);

        ColumnStats targetStat = new ColumnStats("Target", false, false, false, false);
        ColumnValues targetValues = new ColumnValues();
        targetValues.setTotalNumber(getTotalNumber(strTargetMap.values()));
        targetValues.setTotalDistinctNumber(getTotalDistinctNumber(strTargetMap.values()));
        // multiplicity
        Map<String, List<String>> multipleSet = new HashMap<>();
        for (Map.Entry<Publication, List<Marker>> entry : strTargetMap.entrySet()) {
            entry.getValue().forEach(allele1 -> {
                List<String> genes = multipleSet.get(allele1.getZdbID());
                if (genes == null)
                    genes = new ArrayList<>();
                genes.add(entry.getKey().getZdbID());
                multipleSet.put(allele1.getZdbID(), genes);
            });
        }
        targetValues.setMultiplicity(getCardinalityPerUniqueRow(strTargetMap));
        row.put(targetStat, targetValues);


        ColumnStats strStat = new ColumnStats("STR", false, true, false, false);
        ColumnValues strValues = getColumnValuesRowEntity(strTargetMap);
        row.put(strStat, strValues);

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

            ColumnValues colTarget = new ColumnValues();
            colTarget.setTotalNumber(getTotalNumberBase(strTargetMap.get(key), Marker::getAbbreviation));
            colTarget.setTotalDistinctNumber(getTotalDistinctNumber(strTargetMap.get(key), Marker::getAbbreviation));
            //colTarget.setCardinality(getCardinalityPerUniqueRow(List.of(strTargetMap.get(key)), Marker::getAbbreviation));
            statRow.put(targetStat, colTarget);

            ColumnValues colStr = new ColumnValues();
            colStr.setTotalNumber(getTotalNumberBase(strMap.get(key), SequenceTargetingReagent::getAbbreviation));
            colStr.setTotalDistinctNumber(getTotalDistinctNumber(strMap.get(key), SequenceTargetingReagent::getAbbreviation));
            //colTarget.setCardinality(getCardinalityPerUniqueRow(List.of(strMap.get(key)), SequenceTargetingReagent::getAbbreviation));
            statRow.put(strStat, colStr);
/*


            ColumnValues assayStat = new ColumnValues();
            assayStat.setTotalNumber(getTotalNumberMultiValuedBase(strMap.get(pubEntry.getKey()), Antibody::getDistinctAssayNames));
            assayStat.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(strMap.get(pubEntry.getKey()), Antibody::getDistinctAssayNames));
            assayStat.setCardinality(getCardinalityPerUniqueRow(List.of(strMap.get(pubEntry.getKey())), Antibody::getDistinctAssayNames));
            statRow.put(assay, assayStat);
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

    public JsonResultResponse<StatisticRow> getAllPublicationMutation(Pagination pagination) {

        Map<Publication, List<Feature>> publicationMap = new HashMap<>(getPublicationRepository().getAllFeatureFromPublication());

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

        Map<Publication, List<Antibody>> publicationMap = new HashMap<>(getAntibodyRepository().getAntibodiesFromAllPublications());

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
        ColumnStats publicationStat = new ColumnStats("Publication", true, false, false, false);
        ColumnValues columnValues = getColumnValuesUberEntity(publicationMap);
        row.put(publicationStat, columnValues);

        ColumnStats antibodyStat = new ColumnStats("Antibody Name", false, true, false, false);
        ColumnValues antibodyValues = getColumnValuesRowEntity(publicationMap);
        row.put(antibodyStat, antibodyValues);

        ColumnStats antibodyClonalTypeStat = new ColumnStats("Clonal Type", false, false, false, true);
        ColumnValues cloneValues = getColumnValues(publicationMap, Antibody::getClonalType);
        row.put(antibodyClonalTypeStat, cloneValues);

        ColumnStats antibodyIsotypeStat = new ColumnStats("Isotype", false, false, false, true);
        ColumnValues isotypeValues = getColumnValues(publicationMap, Antibody::getHeavyChainIsotype);
        row.put(antibodyIsotypeStat, isotypeValues);

        ColumnStats antibodyHostStat = new ColumnStats("Host Organism", false, false, false, true);
        ColumnValues hostValues = getColumnValues(publicationMap, Antibody::getHostSpecies);
        row.put(antibodyHostStat, hostValues);

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
        sortedMap.entrySet().forEach(pubEntry -> {
            StatisticRow statRow = new StatisticRow();

            ColumnValues colValues = new ColumnValues();
            colValues.setValue(pubEntry.getKey().getZdbID());
            statRow.put(publicationStat, colValues);

            ColumnValues colValueAnti = new ColumnValues();
            colValueAnti.setTotalNumber(pubEntry.getValue());
            statRow.put(antibodyStat, colValueAnti);

            ColumnValues colValueClonaltype = new ColumnValues();
            colValueClonaltype.setTotalNumber(getTotalNumberBase(publicationMap.get(pubEntry.getKey()), Antibody::getClonalType));
            colValueClonaltype.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(pubEntry.getKey()), Antibody::getClonalType));
            statRow.put(antibodyClonalTypeStat, colValueClonaltype);

            ColumnValues isotypeStat = new ColumnValues();
            isotypeStat.setTotalNumber(getTotalNumberBase(publicationMap.get(pubEntry.getKey()), Antibody::getHeavyChainIsotype));
            isotypeStat.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(pubEntry.getKey()), Antibody::getHeavyChainIsotype));
            statRow.put(antibodyIsotypeStat, isotypeStat);

            ColumnValues hostStat = new ColumnValues();
            hostStat.setTotalNumber(getTotalNumberBase(publicationMap.get(pubEntry.getKey()), Antibody::getHostSpecies));
            hostStat.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.get(pubEntry.getKey()), Antibody::getHostSpecies));
            statRow.put(antibodyHostStat, hostStat);

            ColumnValues assayStat = new ColumnValues();
            assayStat.setTotalNumber(getTotalNumberMultiValuedBase(publicationMap.get(pubEntry.getKey()), Antibody::getDistinctAssayNames));
            assayStat.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(publicationMap.get(pubEntry.getKey()), Antibody::getDistinctAssayNames));
            assayStat.setCardinality(getCardinalityPerUniqueRow(List.of(publicationMap.get(pubEntry.getKey())), Antibody::getDistinctAssayNames));
            statRow.put(assay, assayStat);

            ColumnValues labelingStat = new ColumnValues();
            labelingStat.setTotalNumber(getTotalNumberMultiValuedBase(publicationMap.get(pubEntry.getKey()), Antibody::getAntigenGenes));
            labelingStat.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(publicationMap.get(pubEntry.getKey()), Antibody::getAntigenGenes));
            labelingStat.setCardinality(getCardinalityPerUniqueRow(List.of(publicationMap.get(pubEntry.getKey())), Antibody::getAntigenGenes));
            statRow.put(geneStat, labelingStat);

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
        cloneValues.setTotalNumber(getTotalNumberBase(publicationMap.values().stream().flatMap(Collection::stream).collect(toList()), function));
        cloneValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.values().stream().flatMap(Collection::stream).collect(toList()), function));
        cloneValues.setMultiplicity(getCardinalityPerUniqueRow(publicationMap));
        cloneValues.setHistogram(getHistogram(publicationMap, function));
        return cloneValues;
    }

/*
    public TransgenicAlleleStats getAllAlleleVariantStat() {
        Map<String, List<Allele>> alleleMap = cacheRepository.getAllTransgenicAlleles();
        return getAllAlleleVariantStat(alleleMap);
    }
*/


    public void createStatisticRow(StatisticRow row) {
/*
        row..forEach(columnStats -> {
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

    private static <T extends ZdbID, O extends Object> int getTotalNumberBase(List<T> list, Function<T, O> function) {
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

    private static <ID> int getTotalNumberMultiValuedObject(Collection<List<Antibody>> map, Function<Antibody, List<ID>> function) {
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
        return map.stream()
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

    private static <ID> int getTotalDistinctNumberPerUberEntity(List<Antibody> alleles, Function<Antibody, List<ID>> function) {
        return alleles.stream()
                .map(function)
                .flatMap(Collection::stream)
                .collect(toSet())
                .size();
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

}
