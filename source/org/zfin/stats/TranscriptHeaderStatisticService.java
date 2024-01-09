package org.zfin.stats;

import org.apache.commons.text.CaseUtils;
import org.zfin.framework.api.*;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.presentation.TranscriptBean;
import org.zfin.mutant.Genotype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class TranscriptHeaderStatisticService extends GenePageStatisticService<TranscriptBean> {


    public JsonResultResponse<StatisticRow<Marker, TranscriptBean>> getTranscriptHeaderStats(Pagination pagination) {

        Map<Marker, List<TranscriptBean>> geneMapUnfiltered = getMarkerRepository().getAllTranscriptBeans(pagination);
        Map<Marker, List<TranscriptBean>> geneMapFilter = getFilteredMap(geneMapUnfiltered, new TranscriptBeanFiltering(), pagination);
        StatisticRow<Marker, TranscriptBean> row = getMarkerStatisticRowFromEntity(geneMapUnfiltered, geneMapFilter);


        List<ColumnStats<Marker, TranscriptBean>> subEntityColStats = new ArrayList<>();
        subEntityColStats.add(new ColumnStats<>(GeneTranscriptStatisticService.Header.TRANSCRIPT_ID.columnName, false, true, false, false, transcriptBean -> transcriptBean.getTranscript().getZdbID()));
        subEntityColStats.add(new ColumnStats<>(GeneTranscriptStatisticService.Header.TRANSCRIPT_TYPE.columnName, false, false, false, true, transcriptBean -> transcriptBean.getTranscript().getTranscriptType().getType().toString()));
        subEntityColStats.add(new ColumnStats<>(GeneTranscriptStatisticService.Header.TRANSCRIPT_STATUS.columnName, false, false, false, true, transcriptBean -> {
            if (transcriptBean.getTranscript().getStatus() != null) {
                return transcriptBean.getTranscript().getStatus().getStatus().toString();
            }
            return null;
        }));
        subEntityColStats.add(new ColumnStats<>(GeneTranscriptStatisticService.Header.TRANSCRIPT_STRAIN.columnName, false, false, false, true, transcriptBean -> {
            if (transcriptBean.getStrain() != null) {
                return transcriptBean.getStrain().getName();
            }
            return null;
        }));
        subEntityColStats.add(new ColumnStats<>(GeneTranscriptStatisticService.Header.TRANSCRIPT_NON_REF_STRAIN.columnName, false, false, true, true, true, transcriptBean -> {
            Set<Genotype> genotypes = transcriptBean.getNonReferenceStrains();
            if (genotypes == null) return null;
            return genotypes.stream().map(Genotype::getHandle).toList();
        }));
        subEntityColStats.add(new ColumnStats<>(GeneTranscriptStatisticService.Header.TRANSCRIPT_RELATED_GENES.columnName, false, false, true, false, true, transcriptBean -> {
            Set<RelatedMarker> displayGroups = transcriptBean.getRelatedGenes();
            if (displayGroups == null) return null;
            return displayGroups.stream().map(displayGroup -> displayGroup.getMarker().getZdbID()).toList();
        }));

        addSubEntityColumnStatsToStatRow(subEntityColStats, geneMapFilter, geneMapUnfiltered, row);

        // create return result set
        List<StatisticRow<Marker, TranscriptBean>> resultRows = getStatisticResultRows(geneMapFilter, row);
        return getJsonResultResponse(pagination, row, resultRows);
    }

    public enum Header {

        GENE_ID("Gene ID", Marker::getZdbID), GENE_SYMBOL("Gene Symbol", Marker::getAbbreviation), GENE_TYPE("Gene Type", marker -> marker.getMarkerType().getType().toString()), DISPLAY_GROUP("Display Group", null), PLASMID("Plasmid Accession", null);

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

    public static class TranscriptBeanFiltering extends Filtering<TranscriptBean> {


        public TranscriptBeanFiltering() {
            filterFieldMap.put(FieldFilter.STATUS, statusFilter);
            filterFieldMap.put(FieldFilter.NON_REF_STRAIN, nonRefStrainFilter);
        }

        public static FilterFunction<TranscriptBean, String> statusFilter =
            (transcriptBean, value) -> {
                if (transcriptBean.getTranscript().getStatus() != null)
                    return FilterFunction.contains(transcriptBean.getTranscript().getStatus().getStatus().toString(), value);
                else if(value.equals("<empty>"))
                    return transcriptBean.getTranscript().getStatus() == null;
                return false;
            };

        public static FilterFunction<TranscriptBean, String> nonRefStrainFilter = (transcriptBean, value) -> {
            Set<Genotype> genotypes = transcriptBean.getNonReferenceStrains();
            if (genotypes == null) return false;
            return FilterFunction.contains(String.join(",", genotypes.stream().map(Genotype::getHandle).collect(Collectors.toSet())), value);
        };

    }

}
