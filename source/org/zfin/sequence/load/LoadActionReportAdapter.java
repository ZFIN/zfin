package org.zfin.sequence.load;

import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportActionLink;
import org.zfin.datatransfer.report.model.LoadReportMeta;
import org.zfin.datatransfer.report.model.LoadReportSummary;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.datatransfer.report.model.LoadReportTableHeader;
import org.zfin.datatransfer.report.model.ZfinReport;
import org.zfin.report.LegacyReportAdapter;
import org.zfin.report.ReportWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link LoadActionsContainer} into a {@link ZfinReport} so the
 * Priority-Pipeline and Ensembl-Transcript load jobs can render through the
 * unified HTML viewer ({@code source/org/zfin/report/report-template.html}).
 *
 * <p>Sibling to {@link org.zfin.uniprot.UniProtReportAdapter}. The
 * {@link LoadAction} shape is nearly isomorphic to {@code UniProtLoadAction}
 * (type/subType/accession/geneZdbID/details/length/links), so the mapping is
 * essentially identical. The one shape difference is
 * {@link EnsemblLoadSummaryItemDTO}, which carries an unstructured
 * {@code description → count} map instead of explicit before/after rows; we
 * render it as a single two-column table.
 */
public class LoadActionReportAdapter {

    /** Convenience: adapt + render + write — what both task callers want. */
    public void writeHtmlReport(String title, LoadActionsContainer container, File output) throws IOException {
        ZfinReport zfinReport = adapt(title, container);
        new ReportWriter().write(new LegacyReportAdapter().adapt(zfinReport), output);
    }

    public ZfinReport adapt(String title, LoadActionsContainer container) {
        ZfinReport report = new ZfinReport();
        report.setMeta(buildMeta(title));
        report.setSummary(buildSummary(container.getSummary()));
        report.setSupplementalData(Collections.emptyMap());
        report.setActions(buildActions(container.getActions()));
        report.generateAllIds();
        return report;
    }

    private LoadReportMeta buildMeta(String title) {
        LoadReportMeta meta = new LoadReportMeta();
        meta.setTitle(title);
        meta.setCreationDate(System.currentTimeMillis());
        return meta;
    }

    private LoadReportSummary buildSummary(EnsemblLoadSummaryItemDTO dto) {
        String description = "Load summary. Use the tree on the left to drill into added, " +
            "updated, removed, or errored records.";
        List<LoadReportSummaryTable> tables = new ArrayList<>();
        if (dto != null && dto.getCounts() != null && !dto.getCounts().isEmpty()) {
            LoadReportSummaryTable table = new LoadReportSummaryTable();
            table.setDescription(dto.getDescription() != null && !dto.getDescription().isEmpty()
                ? dto.getDescription()
                : "Counts");
            table.setHeaders(List.of(
                new LoadReportTableHeader("name",  "Name"),
                new LoadReportTableHeader("count", "Count")
            ));
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Map.Entry<String, Long> e : dto.getCounts().entrySet()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name",  e.getKey());
                row.put("count", e.getValue() != null ? e.getValue() : 0L);
                rows.add(row);
            }
            table.setRows(rows);
            tables.add(table);
        }
        return new LoadReportSummary(description, tables);
    }

    private List<LoadReportAction> buildActions(Iterable<LoadAction> source) {
        List<LoadReportAction> out = new ArrayList<>();
        if (source == null) return out;
        for (LoadAction a : source) {
            LoadReportAction action = new LoadReportAction();
            action.setType(mapType(a.getType()));
            action.setSubType(a.getSubType() != null ? a.getSubType().getValue() : null);
            action.setAccession(a.getAccession());
            action.setGeneZdbID(a.getGeneZdbID());
            action.setDetails(a.getDetails());
            if (a.getLength() > 0) action.setLength(String.valueOf(a.getLength()));

            if (a.getRelatedEntityFields() != null && !a.getRelatedEntityFields().isEmpty()) {
                action.setRelatedEntityFields(new LinkedHashMap<>(a.getRelatedEntityFields()));
            }

            if (a.getLinks() != null) {
                for (LoadLink link : a.getLinks()) {
                    action.addLink(new LoadReportActionLink(link.title(), link.href()));
                }
            }

            // Same-gene actions get a "related" edge so the viewer's Related
            // panel pairs siblings on the same ZFIN gene.
            if (a.getGeneZdbID() != null && !a.getGeneZdbID().isEmpty()) {
                action.addRelatedActionsKeys("gene:" + a.getGeneZdbID());
            }

            out.add(action);
        }
        return out;
    }

    private static LoadReportAction.Type mapType(LoadAction.Type type) {
        if (type == null) return LoadReportAction.Type.INFO;
        // Name parity between the two enums is verified by a unit test; valueOf
        // throws if a new LoadAction.Type value is added without a matching
        // legacy entry.
        return LoadReportAction.Type.valueOf(type.name());
    }
}
