package org.zfin.stats;

import org.apache.commons.text.CaseUtils;
import org.zfin.framework.api.*;
import org.zfin.marker.Marker;
import org.zfin.sequence.MarkerDBLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GeneDbLinkStatisticService extends GenePageStatisticService<MarkerDBLink> {


    public JsonResultResponse<StatisticRow<Marker, MarkerDBLink>> getPlasmidStats(Pagination pagination) {

        Map<Marker, List<MarkerDBLink>> geneMapUnfiltered = getMarkerRepository().getAllPlasmids(pagination);
        Map<Marker, List<MarkerDBLink>> geneMapFilter = getFilteredMap(geneMapUnfiltered, new PlasmidFiltering(), pagination);

        StatisticRow<Marker, MarkerDBLink> row = getMarkerStatisticRowFromEntity(geneMapUnfiltered, geneMapFilter);

        List<ColumnStats<Marker, MarkerDBLink>> subEntityColStats = new ArrayList<>();
        subEntityColStats.add(new ColumnStats<>(Header.PLASMID.columnName, false, true, false, false, MarkerDBLink::getAccessionNumber));

        addSubEntityColumnStatsToStatRow(subEntityColStats, geneMapFilter, geneMapUnfiltered, row);

        // create return result set
        List<StatisticRow<Marker, MarkerDBLink>> resultRows = getStatisticResultRows(geneMapFilter, row);
        return getJsonResultResponse(pagination, row, resultRows);
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

    public static class PlasmidFiltering extends Filtering<MarkerDBLink> {


        public PlasmidFiltering() {
            filterFieldMap.put(FieldFilter.PLASMID, idFilter);
        }

        public static FilterFunction<MarkerDBLink, String> idFilter =
            (dbLink, value) -> FilterFunction.contains(dbLink.getAccessionNumber(), value);

    }

}
