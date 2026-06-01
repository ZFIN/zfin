package org.zfin.uniprot.secondary;

import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportActionLink;
import org.zfin.datatransfer.report.model.LoadReportMeta;
import org.zfin.datatransfer.report.model.LoadReportSummary;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.datatransfer.report.model.LoadReportTableHeader;
import org.zfin.datatransfer.report.model.ZfinReport;
import org.zfin.uniprot.UniProtLoadLink;
import org.zfin.uniprot.dto.UniProtLoadSummaryItemDTO;
import org.zfin.uniprot.dto.UniProtLoadSummaryListDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link SecondaryTermLoadActionsContainer} into a {@link ZfinReport}
 * so the UniProt secondary-term load (InterPro/EC/PFAM/PROSITE/UniProtKB-KW/PDB
 * link and GO annotation handling) can render through the unified HTML viewer
 * ({@code source/org/zfin/report/report-template.html}).
 *
 * <p>Sibling to {@link org.zfin.uniprot.UniProtReportAdapter}; both produce the
 * same {@code ZfinReport} shape so {@link org.zfin.report.LegacyReportAdapter}
 * groups by Type → SubType the same way for both jobs.
 *
 * <p>Per-action mappings worth calling out:
 * <ul>
 *   <li>{@code dbName} → {@code action.dbName}, also reflected in the action
 *       title via {@link org.zfin.report.LegacyReportAdapter#buildActionTitle}
 *       (geneZdbID — accession).</li>
 *   <li>{@code relatedEntityFields} → flat fields on the action, so things
 *       like the GO term ID/name surface in the field list.</li>
 *   <li>{@code uniprotAccessions} → one {@code supplementalDataKey} per
 *       accession that has a DAT-format blob in {@code uniprotDatFile}, so each
 *       contributing UniProt record renders under the action page.</li>
 * </ul>
 */
public class UniProtSecondaryReportAdapter {

    public ZfinReport adapt(String title, Long releaseID, SecondaryTermLoadActionsContainer container) {
        ZfinReport report = new ZfinReport();
        report.setMeta(buildMeta(title, releaseID, container.getCreationDate()));
        report.setSummary(buildSummary(container.getSummary()));
        report.setSupplementalData(buildSupplementalData(container.getUniprotDatFile()));
        report.setActions(buildActions(container.getActions(), container.getUniprotDatFile()));
        report.generateAllIds();
        return report;
    }

    private LoadReportMeta buildMeta(String title, Long releaseID, java.util.Date creationDate) {
        LoadReportMeta meta = new LoadReportMeta();
        meta.setTitle(title);
        meta.setCreationDate(creationDate != null ? creationDate.getTime() : System.currentTimeMillis());
        if (releaseID != null) {
            meta.setReleaseID(String.valueOf(releaseID));
        }
        return meta;
    }

    private LoadReportSummary buildSummary(UniProtLoadSummaryListDTO summary) {
        String description = "UniProt secondary-term load summary. " +
            "Actions are grouped by type (LOAD/DELETE/etc.) and subtype (DB Link, " +
            "Marker GO Term Evidence, Protein, …). Each action page surfaces the " +
            "contributing UniProt DAT records.";

        List<LoadReportSummaryTable> tables = new ArrayList<>();
        Collection<UniProtLoadSummaryItemDTO> items = summary != null ? summary.values() : null;
        if (items != null && !items.isEmpty()) {
            LoadReportSummaryTable table = new LoadReportSummaryTable();
            table.setDescription("Counts before and after this load");
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

    private List<LoadReportAction> buildActions(List<SecondaryTermLoadAction> source, Map<String, String> datFile) {
        List<LoadReportAction> out = new ArrayList<>();
        if (source == null) return out;
        for (SecondaryTermLoadAction a : source) {
            LoadReportAction action = new LoadReportAction();
            action.setType(mapType(a.getType()));
            action.setSubType(a.getSubType() != null ? a.getSubType().getValue() : null);
            action.setAccession(a.getAccession());
            action.setGeneZdbID(a.getGeneZdbID());
            action.setDetails(a.getDetails());
            action.setRelatedEntityID(a.getRelatedEntityID());
            if (a.getDbName() != null) action.setDbName(a.getDbName().toString());
            if (a.getLength() > 0)     action.setLength(String.valueOf(a.getLength()));

            // relatedEntityFields is a heterogeneous map (GO term IDs/names, PDB
            // structure IDs, etc.). Pass straight through — the renderer lists
            // each entry on the action page as a field.
            if (a.getRelatedEntityFields() != null && !a.getRelatedEntityFields().isEmpty()) {
                action.setRelatedEntityFields(new LinkedHashMap<>(a.getRelatedEntityFields()));
            }

            if (a.getUniprotAccessions() != null && !a.getUniprotAccessions().isEmpty()) {
                action.setUniprotAccessions(new ArrayList<>(a.getUniprotAccessions()));
                if (datFile != null) {
                    List<String> blobRefs = new ArrayList<>();
                    for (String accession : a.getUniprotAccessions()) {
                        if (datFile.containsKey(accession)) blobRefs.add(accession);
                    }
                    if (!blobRefs.isEmpty()) action.setSupplementalDataKeys(blobRefs);
                }
            }

            if (a.getLinks() != null) {
                for (UniProtLoadLink link : a.getLinks()) {
                    action.addLink(new LoadReportActionLink(link.title(), link.href()));
                }
            }
            // Intentionally not calling a.getDynamicLinks(): it resolves ZFIN/InterPro
            // URLs through getSequenceRepository(), which needs Hibernate and would
            // fail at unit-test time. The old Preact viewer only rendered static
            // links anyway, and the unified viewer surfaces same-gene siblings via
            // the related-actions edge below.

            // Same-gene actions get a "related" edge so the viewer's Related
            // panel pairs siblings on the same ZFIN gene.
            if (a.getGeneZdbID() != null && !a.getGeneZdbID().isEmpty()) {
                action.addRelatedActionsKeys("gene:" + a.getGeneZdbID());
            }

            out.add(action);
        }
        return out;
    }

    private static LoadReportAction.Type mapType(SecondaryTermLoadAction.Type type) {
        if (type == null) return LoadReportAction.Type.INFO;
        // Name parity between the two enums is verified by a unit test; valueOf
        // throws if a new value is added without a matching legacy entry.
        return LoadReportAction.Type.valueOf(type.name());
    }

    private static long nullToZero(Long v) { return v == null ? 0L : v; }
}
