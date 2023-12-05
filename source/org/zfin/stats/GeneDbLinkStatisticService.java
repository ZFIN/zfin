package org.zfin.stats;

import org.apache.commons.text.CaseUtils;
import org.zfin.framework.api.*;
import org.zfin.marker.Marker;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.MarkerDBLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.sequence.DisplayGroup.GroupName.OTHER_MARKER_PAGES;

public class GeneDbLinkStatisticService extends GenePageStatisticService<MarkerDBLink> {


    public JsonResultResponse<StatisticRow<Marker, MarkerDBLink>> getPlasmidStats(Pagination pagination) {

        Map<Marker, List<MarkerDBLink>> geneMapUnfiltered = getMarkerRepository().getAllPlasmids(DisplayGroup.GroupName.PLASMIDS, DisplayGroup.GroupName.PATHWAYS, OTHER_MARKER_PAGES);
        Map<Marker, List<MarkerDBLink>> geneMapFilter = getFilteredMap(geneMapUnfiltered, new PlasmidFiltering(), pagination);

        StatisticRow<Marker, MarkerDBLink> row = getMarkerStatisticRowFromEntity(geneMapUnfiltered, geneMapFilter);

        List<ColumnStats<Marker, MarkerDBLink>> subEntityColStats = new ArrayList<>();
        subEntityColStats.add(new ColumnStats<>(Header.PLASMID.columnName, false, true, false, false, MarkerDBLink::getAccessionNumber));
        subEntityColStats.add(new ColumnStats<>(Header.DISPLAY_GROUP.columnName, false, true, true, true, true, markerDBLink -> {
            Set<DisplayGroup> displayGroups = markerDBLink.getReferenceDatabase().getDisplayGroups();
            if (displayGroups == null) return null;
            return displayGroups.stream().map(displayGroup -> displayGroup.getGroupName().name()).toList();
        }));
        subEntityColStats.add(new ColumnStats<>(Header.FOREIGN_DB.columnName, false, true, false, true, markerDBLink -> markerDBLink.getReferenceDatabase().getForeignDB().getDisplayName()));

        addSubEntityColumnStatsToStatRow(subEntityColStats, geneMapFilter, geneMapUnfiltered, row);

        // create return result set
        List<StatisticRow<Marker, MarkerDBLink>> resultRows = getStatisticResultRows(geneMapFilter, row);
        return getJsonResultResponse(pagination, row, resultRows);
    }

    public enum Header {

        GENE_ID("Gene ID", Marker::getZdbID),
        GENE_SYMBOL("Gene Symbol", Marker::getAbbreviation),
        GENE_TYPE("Gene Type", marker -> marker.getMarkerType().getType().toString()),
        DISPLAY_GROUP("Display Group", null),
        PLASMID("Plasmid Accession", null),
        FOREIGN_DB("ExternalDB", null);

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
            filterFieldMap.put(FieldFilter.DISPLAY_GROUP, displayGroupFilter);
        }

        public static FilterFunction<MarkerDBLink, String> idFilter = (dbLink, value) -> FilterFunction.contains(dbLink.getAccessionNumber(), value);

        public static FilterFunction<MarkerDBLink, String> displayGroupFilter = (dbLink, value) -> {
            Set<DisplayGroup> displayGroups = dbLink.getReferenceDatabase().getDisplayGroups();
            if (displayGroups == null) return false;
            return FilterFunction.contains(String.join(",", displayGroups.stream().map(displayGroup -> displayGroup.getGroupName().name()).collect(Collectors.toSet())), value);
        };

    }

}
