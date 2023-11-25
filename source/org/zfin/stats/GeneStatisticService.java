package org.zfin.stats;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.CaseUtils;
import org.zfin.framework.api.*;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.stats.GeneStatisticService.Header.TRANSCRIPT_ID;

public class GeneStatisticService extends StatisticService<Marker> {


    public JsonResultResponse<StatisticRow<Marker,Transcript>> getTranscriptStats(Pagination pagination) {

        Map<Marker, List<Transcript>> geneMapUnfiltered = getMarkerRepository().getAllTranscripts(pagination);

        String geneIDFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.ENTITY_ID);
        Map<Marker, List<Transcript>> entityMap = new HashMap<>(geneMapUnfiltered);
        if (StringUtils.isNotEmpty(geneIDFilterValue)) {
            entityMap.keySet().removeIf(marker -> !marker.getZdbID().contains(geneIDFilterValue));
        }

        String geneSymbolFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.GENE_ABBREVIATION);
        if (StringUtils.isNotEmpty(geneSymbolFilterValue)) {
            entityMap.keySet().removeIf(marker -> !marker.getAbbreviation().contains(geneSymbolFilterValue));
        }

        String geneTypeFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.ZDB_ENTITY_TYPE);
        if (StringUtils.isNotEmpty(geneTypeFilterValue)) {
            entityMap.keySet().removeIf(marker -> !marker.getMarkerType().getType().toString().contains(geneTypeFilterValue));
        }

        FilterService<Transcript> filterService = new FilterService<>(new TranscriptFiltering());
        Map<Marker, List<Transcript>> geneMap = new HashMap<>();
        entityMap.forEach((marker, transcripts) -> {
            List<Transcript> filteredList = filterService.filterAnnotations(transcripts, pagination.getFieldFilterValueMap());
            if (CollectionUtils.isNotEmpty(filteredList)) {
                geneMap.put(marker, filteredList);
            }
        });
        // remove empty marker sets
        Map<Marker, Integer> sortedMap = getSortedEntityMap(geneMap);

        List<ColumnStats<Marker, Transcript>> entityColumnStats = new ArrayList<>();
        entityColumnStats.add(new ColumnStats<>(Header.GENE_ID.columnName, true, false, false, false, Header.GENE_ID.getAttributeFunction()));
        entityColumnStats.add(new ColumnStats<>(Header.GENE_SYMBOL.columnName, true, false, false, false, Header.GENE_SYMBOL.getAttributeFunction()));
        entityColumnStats.add(new ColumnStats<>(Header.GENE_TYPE.columnName, true, false, false, true, Header.GENE_TYPE.getAttributeFunction()));
        StatisticRow<Marker, Transcript> row = addEntityColumnStatsToStatRow(entityColumnStats, geneMap.keySet(), geneMapUnfiltered.keySet());

        Map<ColumnStats<Marker, Transcript>, Function<Transcript, String>> columns = new LinkedHashMap<>();
        columns.put(
            new ColumnStats<>(TRANSCRIPT_ID.columnName, false, true, false, false),
            Transcript::getZdbID);
        columns.put(
            new ColumnStats<>(Header.TRANSCRIPT_TYPE.columnName, false, false, false, true),
            transcript -> transcript.getTranscriptType().getType().toString());

        columns.put(
            new ColumnStats<>(Header.TRANSCRIPT_STATUS.columnName, false, false, false, true),
            transcript -> {
                if (transcript.getStatus() != null) {
                    return transcript.getStatus().getStatus().toString();
                }
                return null;
            });

        // put all columns into a statistic row
        addColumnsToRows(geneMap, row, columns, geneMapUnfiltered);

        // create return result set
        List<StatisticRow<Marker, Transcript>> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow<Marker, Transcript> statRow = new StatisticRow<>();
            row.getColumns().values().stream().filter(column -> column.getColumnDefinition().isSuperEntity())
                .forEach(column -> {
                    ColumnValues colValues = new ColumnValues();
                    colValues.setValue(column.getColumnDefinition().getSingleValueEntityFunction().apply(key));
                    statRow.put(column.getColumnDefinition(), colValues);
                });

            columns.forEach((columnStats, function) -> {
                ColumnValues columnValues = new ColumnValues();
                columnValues.setTotalNumber(getTotalNumberBase(geneMap.get(key), function));
                columnValues.setTotalDistinctNumber(getTotalDistinctNumber(geneMap.get(key), function));
                statRow.put(columnStats, columnValues);
            });

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow<Marker,Transcript>> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    private StatisticRow<Marker, Transcript> addEntityColumnStatsToStatRow(List<ColumnStats<Marker, Transcript>> entityColumnStats, Set<Marker> filteredSet, Set<Marker> unfilteredSet) {
        StatisticRow<Marker, Transcript> row = new StatisticRow<>();
        entityColumnStats.forEach(columnStats -> {
            row.put(columnStats, populateColumnStat(filteredSet, columnStats, unfilteredSet));
        });
        return row;
    }

    protected ColumnValues populateColumnStat(Set<Marker> entitySet, ColumnStats<Marker, Transcript> columnsStats, Set<Marker> unfilteredEntitySet) {
        ColumnValues columnValValues = new ColumnValues();
        columnValValues.setTotalNumber(entitySet.size());
        List<Marker> arrayList = new ArrayList<>(entitySet);
        columnValValues.setTotalDistinctNumber(getTotalDistinctNumberOnObject(List.of(arrayList), columnsStats.getSingleValueEntityFunction()));
        if (columnsStats.isLimitedValues()) {
            Map<String, Integer> filteredHistogram = getHistogramOnUberEntity(entitySet, columnsStats.getSingleValueEntityFunction());
            Map<String, Integer> unfilteredHistogram = getHistogramOnUberEntity(unfilteredEntitySet, columnsStats.getSingleValueEntityFunction());
            populateFilteredCountsOnUnfilteredHistogram(unfilteredHistogram, filteredHistogram);
            columnValValues.setHistogram(unfilteredHistogram);
        }
        return columnValValues;
    }

    public static enum Header {

        GENE_ID("Gene ID", Marker::getZdbID),
        GENE_SYMBOL("Gene Symbol", Marker::getAbbreviation),
        GENE_TYPE("Gene Type", marker -> marker.getMarkerType().getType().toString()),
        TRANSCRIPT_ID("Transcript ID", null),
        TRANSCRIPT_TYPE("Transcript Type", null),
        TRANSCRIPT_STATUS("Transcript Status", null);

        final String columnName;
        final Function<Marker, String> attributeFunction;


        Header(String columnName, Function<Marker, String> function) {
            this.columnName = columnName;
            this.attributeFunction = function;
        }

        public final String getFilterName() {
            return CaseUtils.toCamelCase(columnName, false, ' ');
        }

        public Function<Marker, String> getAttributeFunction() {
            return attributeFunction;
        }
    }

}
