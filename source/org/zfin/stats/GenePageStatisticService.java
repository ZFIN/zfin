package org.zfin.stats;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.text.CaseUtils;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.FilterService;
import org.zfin.framework.api.Filtering;
import org.zfin.framework.api.Pagination;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class GenePageStatisticService<SubEntity extends EntityZdbID> extends StatisticService<Marker, SubEntity> {


    public Map<Marker, List<SubEntity>> getFilteredMap(Map<Marker,
        List<SubEntity>> geneMapUnfiltered,
                                                       Filtering<SubEntity> filtering,
                                                       Pagination pagination) {

        String geneIDFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.ENTITY_ID);
        Map<Marker, List<SubEntity>> entityMap = new HashMap<>(geneMapUnfiltered);
        if (StringUtils.isNotEmpty(geneIDFilterValue)) {
            Predicate<Marker> entityPredicate = marker -> !marker.getZdbID().contains(geneIDFilterValue);
            entityMap.keySet().removeIf(entityPredicate);
        }

        String geneSymbolFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.GENE_ABBREVIATION);
        if (StringUtils.isNotEmpty(geneSymbolFilterValue)) {
            Predicate<Marker> entityPredicate = marker -> !marker.getAbbreviation().contains(geneSymbolFilterValue);
            entityMap.keySet().removeIf(entityPredicate);
        }

        String geneTypeFilterValue = pagination.getFieldFilterValueMap().get(FieldFilter.ZDB_ENTITY_TYPE);
        if (StringUtils.isNotEmpty(geneTypeFilterValue)) {
            Predicate<Marker> entityPredicate = marker -> !marker.getMarkerType().getType().toString().contains(geneTypeFilterValue);
            entityMap.keySet().removeIf(entityPredicate);
        }

        Map<Marker, List<SubEntity>> geneMap = new HashMap<>(entityMap);
        if (filtering != null) {
            geneMap.clear();
            FilterService<SubEntity> filterService = new FilterService<>(filtering);
            entityMap.forEach((marker, subEntity) -> {
                List<SubEntity> filteredList = filterService.filterAnnotations(subEntity, pagination.getFieldFilterValueMap());
                if (CollectionUtils.isNotEmpty(filteredList)) {
                    geneMap.put(marker, filteredList);
                }
            });
        }

        return geneMap;
    }

    protected StatisticRow<Marker, SubEntity> getMarkerStatisticRowFromEntity(Map<Marker, List<SubEntity>> geneMapUnfiltered, Map<Marker, List<SubEntity>> geneMapFilter) {
        List<ColumnStats<Marker, SubEntity>> entityColumnStats = new ArrayList<>();
        entityColumnStats.add(new ColumnStats<>(GeneDbLinkStatisticService.Header.GENE_ID.columnName, GeneDbLinkStatisticService.Header.GENE_ID.getAttributeFunction(), true, false, false, false));
        entityColumnStats.add(new ColumnStats<>(GeneDbLinkStatisticService.Header.GENE_SYMBOL.columnName, GeneDbLinkStatisticService.Header.GENE_SYMBOL.getAttributeFunction(), true, false, false, false));
        entityColumnStats.add(new ColumnStats<>(GeneDbLinkStatisticService.Header.GENE_TYPE.columnName, GeneDbLinkStatisticService.Header.GENE_TYPE.getAttributeFunction(), true, false, false, true));
        StatisticRow<Marker, SubEntity> row = addEntityColumnStatsToStatRow(entityColumnStats, geneMapFilter.keySet(), geneMapUnfiltered.keySet());
        return row;
    }


    public enum Header {

        GENE_ID("Gene ID", Marker::getZdbID),
        GENE_SYMBOL("Gene Symbol", Marker::getAbbreviation),
        GENE_TYPE("Gene Type", marker -> marker.getMarkerType().getType().toString());

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
