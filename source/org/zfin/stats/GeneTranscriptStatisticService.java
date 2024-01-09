package org.zfin.stats;

import org.apache.commons.text.CaseUtils;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.TranscriptFiltering;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class GeneTranscriptStatisticService extends GenePageStatisticService<Transcript> {


    public JsonResultResponse<StatisticRow<Marker, Transcript>> getTranscriptStats(Pagination pagination) {

        Map<Marker, List<Transcript>> geneMapUnfiltered = getMarkerRepository().getAllTranscripts(pagination);
        Map<Marker, List<Transcript>> geneMapFiltered = getFilteredMap(geneMapUnfiltered, new TranscriptFiltering(), pagination);

        StatisticRow<Marker, Transcript> row = getMarkerStatisticRowFromEntity(geneMapUnfiltered, geneMapFiltered);

        List<ColumnStats<Marker, Transcript>> subEntityColStats = new ArrayList<>();
        subEntityColStats.add(new ColumnStats<>(Header.TRANSCRIPT_ID.columnName, false, true, false, false, Transcript::getZdbID));
        subEntityColStats.add(new ColumnStats<>(Header.TRANSCRIPT_TYPE.columnName, false, false, false, true, transcript -> transcript.getTranscriptType().getType().toString()));
        subEntityColStats.add(new ColumnStats<>(Header.TRANSCRIPT_STATUS.columnName, false, false, false, true, transcript -> {
            if (transcript.getStatus() != null) {
                return transcript.getStatus().getStatus().toString();
            }
            return null;
        }));

        addSubEntityColumnStatsToStatRow(subEntityColStats, geneMapFiltered, geneMapUnfiltered, row);

        // create return result set
        List<StatisticRow<Marker, Transcript>> resultRows = getStatisticResultRows(geneMapFiltered, row);
        return getJsonResultResponse(pagination, row, resultRows);
    }

    public enum Header {

        TRANSCRIPT_ID("Transcript ID", null),
        TRANSCRIPT_TYPE("Transcript Type", null),
        TRANSCRIPT_RELATED_GENES("Related Genes", null),
        TRANSCRIPT_STATUS("Transcript Status", null),
        TRANSCRIPT_NON_REF_STRAIN("Transcript Non-Ref Strain", null),
        TRANSCRIPT_STRAIN("Transcript Strain", null);

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
