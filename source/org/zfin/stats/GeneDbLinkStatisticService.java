package org.zfin.stats;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.CaseUtils;
import org.zfin.framework.api.*;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GeneDbLinkStatisticService extends StatisticService<Marker, MarkerDBLink> {



    public JsonResultResponse<StatisticRow<Marker, MarkerDBLink>> getPlasmidStats(Pagination pagination) {

        Map<Marker, List<MarkerDBLink>> geneMapUnfiltered = getMarkerRepository().getAllPlasmids(pagination);

        String geneIDFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.ENTITY_ID);
        Map<Marker, List<MarkerDBLink>> geneMap = new HashMap<>(geneMapUnfiltered);
        if (StringUtils.isNotEmpty(geneIDFilterValue)) {
            geneMap.keySet().removeIf(marker -> !marker.getZdbID().contains(geneIDFilterValue));
        }

        String geneSymbolFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.GENE_ABBREVIATION);
        if (StringUtils.isNotEmpty(geneSymbolFilterValue)) {
            geneMap.keySet().removeIf(marker -> !marker.getAbbreviation().contains(geneSymbolFilterValue));
        }

        String geneTypeFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.ZDB_ENTITY_TYPE);
        if (StringUtils.isNotEmpty(geneTypeFilterValue)) {
            geneMap.keySet().removeIf(marker -> !marker.getMarkerType().getType().toString().contains(geneTypeFilterValue));
        }

        // remove empty marker sets
        Map<Marker, Integer> sortedMap = getSortedEntityMap(geneMap);

        List<ColumnStats<Marker, MarkerDBLink>> entityColumnStats = new ArrayList<>();
        entityColumnStats.add(new ColumnStats<>(Header.GENE_ID.columnName, Header.GENE_ID.getAttributeFunction(), true, false, false, false));
        entityColumnStats.add(new ColumnStats<>(Header.GENE_SYMBOL.columnName, Header.GENE_SYMBOL.getAttributeFunction(), true, false, false, false));
        entityColumnStats.add(new ColumnStats<>(Header.GENE_TYPE.columnName, Header.GENE_TYPE.getAttributeFunction(), true, false, false, true));
        StatisticRow<Marker, MarkerDBLink> row = addEntityColumnStatsToStatRow(entityColumnStats, geneMap.keySet(), geneMapUnfiltered.keySet());

        List<ColumnStats<Marker, MarkerDBLink>> subEntityColStats = new ArrayList<>();
        subEntityColStats.add(new ColumnStats<>(Header.PLASMID.columnName, false, true, false, false, MarkerDBLink::getAccessionNumber));

        addSubEntityColumnStatsToStatRow(subEntityColStats, geneMap, geneMapUnfiltered, row);

        // create return result set
        List<StatisticRow<Marker, MarkerDBLink>> rows = new ArrayList<>();
        sortedMap.forEach((key, value) -> {
            StatisticRow<Marker, MarkerDBLink> statRow = new StatisticRow<>();
            row.getColumns().values().stream().filter(column -> column.getColumnDefinition().isSuperEntity())
                .forEach(column -> {
                    ColumnValues colValues = new ColumnValues();
                    colValues.setValue(column.getColumnDefinition().getSingleValueEntityFunction().apply(key));
                    statRow.put(column.getColumnDefinition(), colValues);
                });

            row.getColumns().values().stream().filter(column -> !column.getColumnDefinition().isSuperEntity())
                .forEach(columnStats -> {
                    ColumnValues columnValues = new ColumnValues();
                    columnValues.setTotalNumber(getTotalNumberBase(geneMap.get(key), columnStats.getColumnDefinition().getSingleValueSubEntityFunction()));
                    columnValues.setTotalDistinctNumber(getTotalDistinctNumber(geneMap.get(key), columnStats.getColumnDefinition().getSingleValueSubEntityFunction()));
                    statRow.put(columnStats.getColumnDefinition(), columnValues);
                });

            rows.add(statRow);
        });

        JsonResultResponse<StatisticRow<Marker, MarkerDBLink>> response = new JsonResultResponse<>();
        response.setResults(rows);
        response.setTotal(rows.size());
        response.setResults(rows.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(toList()));
        response.addSupplementalData("statistic", row);
        return response;
    }

    public enum Header {

        GENE_ID("Gene ID", Marker::getZdbID),
        GENE_SYMBOL("Gene Symbol", Marker::getAbbreviation),
        GENE_TYPE("Gene Type", marker -> marker.getMarkerType().getType().toString()),
        PLASMID("Plasmid Accession", null);

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
