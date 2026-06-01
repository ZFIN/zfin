package org.zfin.uniprot;

import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportActionLink;
import org.zfin.datatransfer.report.model.LoadReportActionTag;
import org.zfin.datatransfer.report.model.LoadReportMeta;
import org.zfin.datatransfer.report.model.LoadReportSummary;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.datatransfer.report.model.LoadReportTableHeader;
import org.zfin.datatransfer.report.model.ZfinReport;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link UniProtLoadActionsContainer} into a {@link ZfinReport} so the
 * UniProt diff load can render through the same HTML viewer used by the
 * NCBI-Gene-Load-Java and GAF/GOA jobs ({@code source/org/zfin/report/report-template.html}).
 *
 * <p>Mapping:
 * <ul>
 *   <li>{@code summary} list → one summary table on the root node
 *       (Description / Before / After / Net Change / Percent Change).</li>
 *   <li>{@code uniprotDatFile} map → top-level supplementalData blobs keyed by
 *       accession. Each action references its accession via supplementalDataKeys,
 *       so the renderer surfaces the raw DAT-format record under the action.</li>
 *   <li>{@code actions} → {@link LoadReportAction} per action, grouped by
 *       Type → SubType by {@link org.zfin.report.LegacyReportAdapter}.</li>
 * </ul>
 */
public class UniProtReportAdapter {

    public ZfinReport adapt(String title, String releaseID, UniProtLoadActionsContainer container) {
        ZfinReport report = new ZfinReport();
        report.setMeta(buildMeta(title, releaseID));
        report.setSummary(buildSummary(container.getSummary()));
        report.setSupplementalData(buildSupplementalData(container.getUniprotDatFile()));
        report.setActions(buildActions(container.getActions(), container.getUniprotDatFile()));
        report.generateAllIds();
        return report;
    }

    private LoadReportMeta buildMeta(String title, String releaseID) {
        LoadReportMeta meta = new LoadReportMeta();
        meta.setTitle(title);
        meta.setCreationDate(System.currentTimeMillis());
        if (releaseID != null && !releaseID.isEmpty()) {
            meta.setReleaseID(releaseID);
        }
        return meta;
    }

    private LoadReportSummary buildSummary(List<UniProtLoadSummaryItemDTO> items) {
        String description = "UniProt diff load summary. " +
            "Use the tree on the left to drill into accessions that will be loaded, " +
            "deleted, ignored, or flagged with warnings/errors.";

        List<LoadReportSummaryTable> tables = new ArrayList<>();
        if (items != null && !items.isEmpty()) {
            LoadReportSummaryTable table = new LoadReportSummaryTable();
            table.setDescription("db_link counts before and after this load");
            table.setHeaders(List.of(
                new LoadReportTableHeader("description", "Type"),
                new LoadReportTableHeader("before",      "# Before"),
                new LoadReportTableHeader("after",       "# After"),
                new LoadReportTableHeader("change",      "Net change"),
                new LoadReportTableHeader("percent",     "% change")
            ));
            List<Map<String, Object>> rows = new ArrayList<>();
            for (UniProtLoadSummaryItemDTO item : items) {
                long before = nullToZero(item.beforeLoadCount());
                long after  = nullToZero(item.afterLoadCount());
                long change = after - before;
                String percent = before == 0
                    ? ""
                    : String.format("%.2f%%", (change * 100.0) / before);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("description", item.description());
                row.put("before",      before);
                row.put("after",       after);
                row.put("change",      change);
                row.put("percent",     percent);
                rows.add(row);
            }
            table.setRows(rows);
            tables.add(table);
        }
        return new LoadReportSummary(description, tables);
    }

    private Map<String, Object> buildSupplementalData(Map<String, String> datFile) {
        Map<String, Object> supplemental = new HashMap<>();
        if (datFile != null) supplemental.putAll(datFile);
        return supplemental;
    }

    private List<LoadReportAction> buildActions(Iterable<UniProtLoadAction> source, Map<String, String> datFile) {
        List<LoadReportAction> out = new ArrayList<>();
        if (source == null) return out;
        for (UniProtLoadAction a : source) {
            LoadReportAction action = new LoadReportAction();
            action.setType(mapType(a.getType()));
            action.setSubType(a.getSubType() != null ? a.getSubType().getValue() : null);
            action.setAccession(a.getAccession());
            action.setGeneZdbID(a.getGeneZdbID());
            action.setDetails(a.getDetails());
            if (a.getLength() > 0) action.setLength(String.valueOf(a.getLength()));

            if (a.getLinks() != null) {
                for (UniProtLoadLink link : a.getLinks()) {
                    action.addLink(new LoadReportActionLink(link.title(), link.href()));
                }
            }

            if (a.getTags() != null) {
                for (UniProtLoadTag tag : a.getTags()) {
                    action.addTag(new LoadReportActionTag(tag.name(), tag.value()));
                }
            }

            // Surface the raw DAT-format record under the action page, matching the
            // <pre>${uniprotRecord}</pre> behaviour of the legacy load-report.html.
            if (a.getAccession() != null && datFile != null && datFile.containsKey(a.getAccession())) {
                action.setSupplementalDataKeys(List.of(a.getAccession()));
            }

            // Same-gene actions get a "related" edge so the viewer's Related panel
            // links sibling accessions on the same ZFIN gene.
            if (a.getGeneZdbID() != null && !a.getGeneZdbID().isEmpty()) {
                action.addRelatedActionsKeys("gene:" + a.getGeneZdbID());
            }

            out.add(action);
        }
        return out;
    }

    private static LoadReportAction.Type mapType(UniProtLoadAction.Type type) {
        if (type == null) return LoadReportAction.Type.INFO;
        // Names are identical between the two enums for every UniProt value, so
        // the by-name lookup is exact; valueOf throws if a new UniProt type is
        // added without a matching legacy enum entry, which is what we want.
        return LoadReportAction.Type.valueOf(type.name());
    }

    private static long nullToZero(Long v) { return v == null ? 0L : v; }
}
