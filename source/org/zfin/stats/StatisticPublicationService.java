package org.zfin.stats;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;
import org.zfin.antibody.Antibody;
import org.zfin.framework.api.*;
import org.zfin.publication.Publication;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;

public class StatisticPublicationService {

    public JsonResultResponse<StatisticRow> getAllPublicationAntibodies(Pagination pagination) {
        String species = pagination.getFieldFilterValueMap().get(FieldFilter.SPECIES);

        // filter by sub entity
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

        // default sorting: number of entities
        HashMap<Publication, Integer> integerMap = publicationMap.entrySet().stream()
                .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().size()), HashMap::putAll);
        Map<Publication, Integer> sortedMap = integerMap.entrySet().
                stream().
                sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


        StatisticRow row = new StatisticRow();
        ColumnStats publicationStat = new ColumnStats("Publication", true, false, false, false);
        ColumnValues columnValues = new ColumnValues();
        columnValues.setTotalNumber(publicationMap.size());
        row.put(publicationStat, columnValues);

        ColumnStats antibodyStat = new ColumnStats("Antibody Name", false, true, false, false);
        ColumnValues antibodyValues = new ColumnValues();
        antibodyValues.setTotalNumber(getTotalNumber(publicationMap.values()));
        antibodyValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.values()));
        // multiplicity
        Map<String, List<String>> multipleSet = new HashMap<>();
        for (Map.Entry<Publication, List<Antibody>> entry : publicationMap.entrySet()) {
            entry.getValue().forEach(allele1 -> {
                List<String> genes = multipleSet.get(allele1.getZdbID());
                if (genes == null)
                    genes = new ArrayList<>();
                genes.add(entry.getKey().getZdbID());
                multipleSet.put(allele1.getZdbID(), genes);
            });
        }
        antibodyValues.setMultiplicity(getCardinalityPerUniqueRow(publicationMap));
        row.put(antibodyStat, antibodyValues);

        //ColumnStats<Antibody, ?> alleleColStat = getRowEntityColumn();

        ColumnStats antibodyClonalTypeStat = new ColumnStats("Clonal Type", false, false, false, false);
        ColumnValues cloneValues = getColumnValues(publicationMap, Antibody::getClonalType, Antibody::getClonalType);
        row.put(antibodyClonalTypeStat, cloneValues);

        ColumnStats antibodyIsotypeStat = new ColumnStats("Isotype", false, false, false, false);
        ColumnValues isotypeValues = getColumnValues(publicationMap, Antibody::getHeavyChainIsotype, Antibody::getHeavyChainIsotype);
        row.put(antibodyIsotypeStat, isotypeValues);

        ColumnStats antibodyHostStat = new ColumnStats("Host Organism", false, false, false, false);
        ColumnValues hostValues = getColumnValues(publicationMap, Antibody::getHostSpecies, Antibody::getHostSpecies);
        row.put(antibodyHostStat, hostValues);

        ColumnStats assay = new ColumnStats("Assay", false, false, true, false);
        ColumnValues assayValues = new ColumnValues();
        assayValues.setTotalNumber(getTotalNumberMultiValued(publicationMap.values(), Antibody::getDistinctAssayNames));
        assayValues.setTotalDistinctNumber(getTotalDistinctNumber(publicationMap.values(), Antibody::getDistinctAssayNames));
        assayValues.setHistogram(getHistogramMultiValuedOnUniqueRow(publicationMap.values(), Antibody::getDistinctAssayNames));
        assayValues.setCardinality(getCardinalityPerUniqueRow(publicationMap.values(), Antibody::getDistinctAssayNames));
        assayValues.setMultiplicity(getCardinalityPerUniqueRow(publicationMap.values(), Antibody::getDistinctAssayNames));
        cloneValues.setUberHistogram(getHistogramMultiValued(publicationMap, Antibody::getDistinctAssayNames));
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
            colValueClonaltype.setTotalDistinctNumber(getTotalDistinctNumberOnObjectBase(publicationMap.get(pubEntry.getKey()), Antibody::getClonalType));
            statRow.put(antibodyClonalTypeStat, colValueClonaltype);

            ColumnValues isotypeStat = new ColumnValues();
            isotypeStat.setTotalNumber(getTotalNumberBase(publicationMap.get(pubEntry.getKey()), Antibody::getHeavyChainIsotype));
            isotypeStat.setTotalDistinctNumber(getTotalDistinctNumberOnObjectBase(publicationMap.get(pubEntry.getKey()), Antibody::getHeavyChainIsotype));
            statRow.put(antibodyIsotypeStat, isotypeStat);

            ColumnValues hostStat = new ColumnValues();
            hostStat.setTotalNumber(getTotalNumberBase(publicationMap.get(pubEntry.getKey()), Antibody::getHostSpecies));
            hostStat.setTotalDistinctNumber(getTotalDistinctNumberOnObjectBase(publicationMap.get(pubEntry.getKey()), Antibody::getHostSpecies));
            statRow.put(antibodyHostStat, hostStat);

            ColumnValues assayStat = new ColumnValues();
            assayStat.setTotalNumber(getTotalNumberMultiValuedBase(publicationMap.get(pubEntry.getKey()), Antibody::getDistinctAssayNames));
            assayStat.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(publicationMap.get(pubEntry.getKey()), Antibody::getDistinctAssayNames));
            statRow.put(assay, assayStat);

            ColumnValues labelingStat = new ColumnValues();
            labelingStat.setTotalNumber(getTotalNumberMultiValuedBase(publicationMap.get(pubEntry.getKey()), Antibody::getAntigenGenes));
            labelingStat.setTotalDistinctNumber(getTotalDistinctNumberPerUberEntity(publicationMap.get(pubEntry.getKey()), Antibody::getAntigenGenes));
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


    private ColumnValues getColumnValues(Map<Publication, List<Antibody>> publicationMap, Function<Antibody, Object> objectFunction, Function<Antibody, String> stringFunction) {
        ColumnValues cloneValues = new ColumnValues();
        cloneValues.setTotalNumber(getTotalNumberBase(publicationMap.values().stream().flatMap(Collection::stream).collect(toList()), objectFunction));
        cloneValues.setTotalDistinctNumber(getTotalDistinctNumberOnObjectBase(publicationMap.values().stream().flatMap(Collection::stream).collect(toList()), objectFunction));
        cloneValues.setMultiplicity(getCardinalityPerUniqueRow(publicationMap));
        cloneValues.setHistogram(getHistogram(publicationMap, stringFunction));
        return cloneValues;
    }

/*
    public TransgenicAlleleStats getAllAlleleVariantStat() {
        Map<String, List<Allele>> alleleMap = cacheRepository.getAllTransgenicAlleles();
        return getAllAlleleVariantStat(alleleMap);
    }
*/

/*
    public TransgenicAlleleStats getAllAlleleVariantStat(final Map<String, List<Allele>> alleleMap) {

        TransgenicAlleleStats stat = new TransgenicAlleleStats();

        ColumnStats gene = new ColumnStats("Gene", true, false, false, false);
        ColumnValues geneValues = new ColumnValues();
        geneValues.setTotalNumber(alleleMap.size());
        geneValues.setTotalDistinctNumber(alleleMap.size());
        stat.addColumn(gene, geneValues);

        ColumnStats geneSpecies = new ColumnStats("Gene Species", true, false, false, true);

        List<String> species = alleleMap.keySet().stream()
                .map(pk -> SpeciesType.getTypeByID(pk.split("\\|\\|")[2]).getAbbreviation())
                .collect(toList());
        Map<String, List<String>> sortSpecies = species.stream()
                .collect(groupingBy(o -> o));

        ColumnValues geneSpeciesValues = new ColumnValues();
        geneSpeciesValues.setHistogram(getValueSortedMap(sortSpecies));
        geneSpeciesValues.setTotalDistinctNumber(getValueSortedMap(sortSpecies).size());
        stat.addColumn(geneSpecies, geneSpeciesValues);

        ColumnStats allele = new ColumnStats("Allele Symbol", false, true, false, false);
        ColumnValues alleleValues = new ColumnValues();
        alleleValues.setTotalNumber(alleleMap.values().stream().mapToInt(List::size).sum());
        List<String> distinctAlleles = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(GeneticEntity::getPrimaryKey)
                .distinct()
                .collect(toList());
        alleleValues.setTotalDistinctNumber(distinctAlleles.size());
        // multiplicity
        Map<String, List<String>> multipleSet = new HashMap<>();
        for (Map.Entry<String, List<Allele>> entry : alleleMap.entrySet()) {
            entry.getValue().forEach(allele1 -> {
                List<String> genes = multipleSet.get(allele1.getPrimaryKey());
                if (genes == null)
                    genes = new ArrayList<>();
                genes.add(entry.getKey());
                multipleSet.put(allele1.getPrimaryKey(), genes);
            });
        }
        stat.addColumn(allele, alleleValues);

        ColumnStats synonym = new ColumnStats("Synonym", false, false, true, false);
        ColumnValues synonymValues = new ColumnValues();
        synonymValues.setTotalNumber(getTotalNumber(alleleMap, Allele::getSynonyms));
        synonymValues.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, Allele::getSynonymList));
        synonymValues.setCardinality(getCardinality(alleleMap, Allele::getSynonymList));
        stat.addColumn(synonym, synonymValues);

        ColumnStats tg = new ColumnStats("Variant", false, false, true, false);
        ColumnValues tgValues = new ColumnValues();
        tgValues.setTotalNumber(getTotalNumber(alleleMap, Allele::getVariants));
        tgValues.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, Allele::getVariants));
        tgValues.setCardinality(getCardinality(alleleMap, Allele::getVariants));
        stat.addColumn(tg, tgValues);

        ColumnStats diseaseStat = new ColumnStats("Associated Human Disease", false, false, false, true);
        ColumnValues diseaseValues = new ColumnValues();
        Function<Allele, String> diseaseFunction = feature -> feature.hasDisease().toString();
*/
/*
        diseaseStat.setTotalNumber(getTotalNumber(alleleMap, disease));
        diseaseStat.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, disease));
        diseaseStat.setCardinality(getCardinality(alleleMap, disease));
*//*

        diseaseValues.setHistogram(getHistogram(alleleMap, diseaseFunction));
        stat.addColumn(diseaseStat, diseaseValues);

        ColumnStats phenotypeStat = new ColumnStats("Associated Phenotype", false, false, false, true);
        ColumnValues phenotypeValues = new ColumnValues();
        Function<Allele, String> phenotypeFunction = feature -> feature.hasPhenotype().toString();
*/
/*
        diseaseStat.setTotalNumber(getTotalNumber(alleleMap, disease));
        diseaseStat.setTotalDistinctNumber(getTotalDistinctNumber(alleleMap, disease));
        diseaseStat.setCardinality(getCardinality(alleleMap, disease));
*//*

        phenotypeValues.setHistogram(getHistogram(alleleMap, phenotypeFunction));
        stat.addColumn(phenotypeStat, phenotypeValues);

        return stat;
    }
*/

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

    private static <ID> Range getCardinalityPerUniqueRow(Map<Publication, List<Antibody>> publicationMap) {
        Map<String, Integer> cardinality = publicationMap.entrySet().stream()
                .collect(toMap(entry -> entry.getKey().getZdbID(), entry -> entry.getValue().size()));
        if (MapUtils.isNotEmpty(cardinality))
            return Range.between(Collections.max(cardinality.values()), Collections.min(cardinality.values()));
        else return null;
    }

    private static <ID> Range getCardinalityPerUniqueRow(Collection<List<Antibody>> map, Function<Antibody, List<ID>> function) {
        Map<String, Integer> cardinality = map.stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(toMap(Antibody::getZdbID,
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

    private static int getTotalNumber(Collection<List<Antibody>> map) {
        return map.stream()
                .mapToInt(List::size).sum();
    }

    private static int getTotalNumberBase(List<Antibody> list, Function<Antibody, Object> function) {
        return (int) list.stream()
                .filter(o -> function.apply(o) != null)
                .count();
    }

    private static int getTotalNumberMultiValued(Collection<List<Antibody>> map, Function<Antibody, List<String>> function) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(function)
                .mapToInt(List::size).sum();
    }

    private static int getTotalNumberMultiValuedBase(List<Antibody> map, Function<Antibody, List<? extends Object>> function) {
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
    private static long getTotalDistinctNumber(Collection<List<Antibody>> map) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(Antibody::getZdbID)
                .distinct()
                .count();
    }

    private static <ID> long getTotalDistinctNumber(Collection<List<Antibody>> map, Function<Antibody, List<ID>> function) {
        return map.stream()
                .flatMap(Collection::stream)
                .map(function)
                .distinct()
                .count();
    }

    private static <ID> long getTotalDistinctNumberBase(List<Antibody> map, Function<Antibody, List<ID>> function) {
        return map.stream()
                .map(function)
                .distinct()
                .count();
    }

    private static long getTotalDistinctNumberOnObjectBase(List<Antibody> map, Function<Antibody, Object> function) {
        return map.stream()
                .map(function)
                .distinct()
                .count();
    }

    private static long getTotalDistinctNumberOnObject(Collection<List<Antibody>> map, Function<Antibody, Object> function) {
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

    private static Map<String, Integer> getHistogram(Map<Publication, List<Antibody>> alleleMap, Function<Antibody, String> function) {
        Map<String, List<String>> histogramRaw = alleleMap.values().stream()
                .flatMap(Collection::stream)
                .map(function)
                .collect(toList())
                .stream()
                .collect(groupingBy(o -> {
                    if (o == null)
                        return "";
                    return o;
                }));
        return getValueSortedMap(histogramRaw);
    }

    private static Map<String, Integer> getHistogramMultiValued(Map<Publication, List<Antibody>> alleleMap, Function<Antibody, List<String>> function) {
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

    private static Map<String, Integer> getHistogramMultiValuedOnUniqueRow(Collection<List<Antibody>> antibodies, Function<Antibody, List<String>> function) {
        Map<String, List<String>> histogramRaw = antibodies.stream()
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
