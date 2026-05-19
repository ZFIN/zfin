package org.zfin.report;

import org.zfin.datatransfer.report.model.LoadReportAction;
import org.zfin.datatransfer.report.model.LoadReportActionLink;
import org.zfin.datatransfer.report.model.LoadReportActionTag;
import org.zfin.datatransfer.report.model.LoadReportSummaryTable;
import org.zfin.datatransfer.report.model.LoadReportTableHeader;
import org.zfin.datatransfer.report.model.ZfinReport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts a legacy {@link ZfinReport} (flat action list with type/subType)
 * into the new {@link Report} (recursive node tree). Lets producers like
 * {@code NCBIReportBuilder} adopt the new viewer without changing how they
 * collect their data.
 *
 * <p>Mapping:
 * <ul>
 *   <li>{@code meta.title}             → {@code Report.meta.title}</li>
 *   <li>{@code meta.releaseID}         → {@code Report.meta.subtitle} (when non-empty)</li>
 *   <li>{@code meta.creationDate}      → {@code Report.meta.createdAt}</li>
 *   <li>{@code summary.description}    → root {@code body}</li>
 *   <li>{@code summary.tables}         → root {@code tables}</li>
 *   <li>{@code supplementalData}       → top-level {@code blobs}</li>
 *   <li>{@code actions}                → grouped by {@code type} → {@code subType} → action leaf
 *       (typeNode and subTypeNode are structural; action leaves carry fields/body/tables/links/tags)</li>
 *   <li>{@code action.relatedActionsKeys} → top-level {@link Report.Edge}
 *       entries. For every key shared by ≤ {@link #MAX_ACTIONS_PER_RELATED_KEY}
 *       actions, all pairs of those actions get a deduped "related" edge so
 *       the viewer's Related section surfaces siblings on the same
 *       gene/accession. Keys that span more actions than the cap are skipped.</li>
 * </ul>
 *
 * <p>When a subType group contains exactly one action, the intermediate
 * subType wrapper is folded into the action node directly: the user clicks
 * the subType title and lands on its content, instead of a one-row table
 * pointing to a single child.
 */
public class LegacyReportAdapter {

    /**
     * Above this size, related-actions edges aren't emitted for a key.
     * Edges grow O(N²) per key, and a "celebrity" gene shared by hundreds
     * of actions would balloon the report; the user can still find those
     * actions via the subType list. 50 keeps the typical case well-covered
     * (the long tail of small clusters) without runaway HTML size.
     */
    private static final int MAX_ACTIONS_PER_RELATED_KEY = 50;

    /**
     * Above this many k:v lines, the details parser bails out so a
     * runaway/non-k:v body falls back to plain text rendering instead of
     * producing a giant table.
     */
    private static final int MAX_KV_LINES = 1000;

    public Report adapt(ZfinReport legacy) {
        Report report = new Report()
            .meta(adaptMeta(legacy))
            .definitions(buildDefinitions());

        if (legacy.getSupplementalData() != null) {
            for (Map.Entry<String, Object> e : legacy.getSupplementalData().entrySet()) {
                if (e.getValue() != null) report.addBlob(e.getKey(), String.valueOf(e.getValue()));
            }
        }

        ReportNode root = new ReportNode().id("_root");
        if (legacy.getMeta() != null && legacy.getMeta().getTitle() != null) {
            root.title(legacy.getMeta().getTitle());
        }
        if (legacy.getSummary() != null) {
            String desc = legacy.getSummary().getDescription();
            if (desc != null && !desc.isEmpty()) root.body(Report.Body.text(desc));
            if (legacy.getSummary().getTables() != null) {
                for (LoadReportSummaryTable t : legacy.getSummary().getTables()) {
                    root.addTable(adaptTable(t));
                }
            }
        }

        Map<String, Map<String, List<LoadReportAction>>> grouped = groupActions(legacy.getActions());
        for (Map.Entry<String, Map<String, List<LoadReportAction>>> typeEntry : grouped.entrySet()) {
            String type = typeEntry.getKey();
            ReportNode typeNode = new ReportNode()
                .id("type-" + slugify(type))
                .title(type)
                .categoryRef(type);
            int typeTotal = 0;
            int subIdx = 0;
            for (Map.Entry<String, List<LoadReportAction>> stEntry : typeEntry.getValue().entrySet()) {
                String subType = stEntry.getKey();
                List<LoadReportAction> actions = stEntry.getValue();
                String subBaseId = "type-" + slugify(type) + "-st-" + subIdx++ + "-" + slugify(subType);

                if (actions.size() == 1) {
                    // Skip the intermediate "list of one" — clicking the subType title
                    // should land directly on the content. Re-title with the subType
                    // since the action's accession-based title is often "N/A — N/A"
                    // for REPORTS-style actions that aren't gene-scoped.
                    ReportNode merged = adaptAction(actions.get(0), type, subBaseId);
                    merged.title(subType);
                    if (merged.getCount() == null) merged.count(1);
                    typeNode.addChild(merged);
                    typeTotal += 1;
                } else {
                    ReportNode subTypeNode = new ReportNode()
                        .id(subBaseId)
                        .title(subType)
                        .categoryRef(type)
                        .count(actions.size());
                    int actionIdx = 0;
                    for (LoadReportAction a : actions) {
                        subTypeNode.addChild(adaptAction(a, type, subBaseId + "-a-" + actionIdx++));
                    }
                    typeNode.addChild(subTypeNode);
                    typeTotal += actions.size();
                }
            }
            typeNode.count(typeTotal);
            root.addChild(typeNode);
        }

        report.root(root);
        addRelatedEdges(report, legacy.getActions());
        return report;
    }

    /**
     * Build "related" edges from {@code relatedActionsKeys}. For every shared key
     * with at most {@link #MAX_ACTIONS_PER_RELATED_KEY} actions, every pair of
     * those actions gets one deduped edge — the renderer's Related section
     * surfaces it on both endpoints (incoming + outgoing).
     */
    private void addRelatedEdges(Report report, List<LoadReportAction> actions) {
        if (actions == null) return;
        // Map each key to the action ids that carry it.
        Map<String, List<String>> keyToIds = new LinkedHashMap<>();
        for (LoadReportAction a : actions) {
            List<String> keys = a.getRelatedActionsKeys();
            if (keys == null || keys.isEmpty()) continue;
            String id = a.getId() != null ? String.valueOf(a.getId()) : null;
            if (id == null) continue;
            for (String k : keys) {
                keyToIds.computeIfAbsent(k, x -> new ArrayList<>()).add(id);
            }
        }
        // Tuple-based dedup so two ids that happen to contain the same
        // separator characters can't collide with a different pair.
        Set<Map.Entry<String, String>> seen = new HashSet<>();
        for (List<String> ids : keyToIds.values()) {
            if (ids.size() < 2 || ids.size() > MAX_ACTIONS_PER_RELATED_KEY) continue;
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    String a = ids.get(i), b = ids.get(j);
                    if (a.equals(b)) continue;
                    Map.Entry<String, String> pair = a.compareTo(b) < 0
                        ? Map.entry(a, b)
                        : Map.entry(b, a);
                    if (seen.add(pair)) {
                        report.addEdge(a, b, "related");
                    }
                }
            }
        }
    }

    private Report.Meta adaptMeta(ZfinReport legacy) {
        Report.Meta meta = new Report.Meta().schemaVersion("1");
        if (legacy.getMeta() != null) {
            meta.title(legacy.getMeta().getTitle());
            meta.createdAt(legacy.getMeta().getCreationDate());
            String releaseID = legacy.getMeta().getReleaseID();
            if (releaseID != null && !releaseID.isEmpty()) {
                meta.subtitle("Release: " + releaseID);
            }
        }
        return meta;
    }

    /** Defines the legacy action-type enum values as categories. */
    private Report.Definitions buildDefinitions() {
        return new Report.Definitions()
            .category("LOAD",    new Report.CategoryDef().label("Load").icon("✅").order(1))
            .category("UPDATE",  new Report.CategoryDef().label("Update").icon("✏️").order(2))
            .category("DELETE",  new Report.CategoryDef().label("Delete").icon("🗑").order(3))
            .category("ERROR",   new Report.CategoryDef().label("Error").icon("❌").order(4))
            .category("WARNING", new Report.CategoryDef().label("Warning").icon("⚠️").order(5))
            .category("INFO",    new Report.CategoryDef().label("Info").icon("ℹ️").order(6))
            .category("DUPES",   new Report.CategoryDef().label("Duplicates").icon("📑").order(7))
            .category("IGNORE",  new Report.CategoryDef().label("Ignored").icon("·").order(8))
            .category("REPORTS", new Report.CategoryDef().label("Reports").icon("📊").order(9));
    }

    private ReportNode adaptAction(LoadReportAction a, String typeRef, String idFallback) {
        String id = a.getId() != null ? String.valueOf(a.getId()) : idFallback;
        ReportNode n = new ReportNode()
            .id(id)
            .title(buildActionTitle(a))
            .categoryRef(typeRef);

        // Producers sometimes set placeholder "N/A" strings for accession/geneZdbID
        // on actions that aren't gene-scoped (e.g. REPORTS category). Treat those
        // as absent so the rendered field list stays clean.
        String accession = nullIfPlaceholder(a.getAccession());
        String geneZdbID = nullIfPlaceholder(a.getGeneZdbID());

        if (accession != null)               n.field("accession", accession);
        if (geneZdbID != null)               n.field("geneZdbID", geneZdbID);
        if (a.getDbName() != null)           n.field("database", a.getDbName());
        if (a.getMd5() != null)              n.field("md5", a.getMd5());
        if (a.getLength() != null)           n.field("length", a.getLength());
        if (a.getRelatedEntityID() != null)  n.field("relatedEntityID", a.getRelatedEntityID());
        if (a.getRelatedEntityFields() != null) {
            for (Map.Entry<String, Object> e : a.getRelatedEntityFields().entrySet()) {
                n.field(e.getKey(), e.getValue());
            }
        }
        if (a.getUniprotAccessions() != null && !a.getUniprotAccessions().isEmpty()) {
            n.field("uniprotAccessions", String.join(", ", a.getUniprotAccessions()));
        }

        if (a.getDetails() != null && !a.getDetails().isEmpty()) {
            ReportTable kv = tryParseKeyValueTable(a.getDetails());
            if (kv != null) {
                n.addTable(kv);
            } else {
                n.body(Report.Body.text(a.getDetails()));
            }
        }

        if (a.getTables() != null) {
            for (LoadReportSummaryTable t : a.getTables()) {
                n.addTable(adaptTable(t));
            }
        }

        if (a.getLinks() != null) {
            for (LoadReportActionLink l : a.getLinks()) {
                if (l != null && l.getTitle() != null && l.getHref() != null) {
                    n.addLink(l.getTitle(), l.getHref());
                }
            }
        }

        if (a.getTags() != null) {
            for (LoadReportActionTag t : a.getTags()) {
                if (t != null && t.getName() != null) n.addTag(t.getName());
            }
        }

        if (a.getSupplementalDataKeys() != null) {
            for (String k : a.getSupplementalDataKeys()) n.addBlobRef(k);
        }

        return n;
    }

    private ReportTable adaptTable(LoadReportSummaryTable t) {
        ReportTable table = new ReportTable();
        if (t.getDescription() != null) table.title(t.getDescription());
        if (t.getHeaders() != null) {
            for (LoadReportTableHeader h : t.getHeaders()) {
                table.addColumn(ReportTable.Column.of(h.getKey(), h.getTitle()));
            }
        }
        if (t.getRows() != null) {
            for (Map<String, Object> row : t.getRows()) {
                table.addRow(new LinkedHashMap<>(row));
            }
        }
        return table;
    }

    private static String buildActionTitle(LoadReportAction a) {
        String accession = nullIfPlaceholder(a.getAccession());
        String geneZdbID = nullIfPlaceholder(a.getGeneZdbID());
        if (accession != null && geneZdbID != null) {
            return geneZdbID + " — " + accession;
        }
        if (accession != null) return accession;
        if (geneZdbID != null) return geneZdbID;
        return a.getSubType() != null ? a.getSubType() : "Item";
    }

    /** Treat empty/null/whitespace and "N/A" placeholders as absent. */
    private static String nullIfPlaceholder(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || "N/A".equalsIgnoreCase(t)) return null;
        return s;
    }

    /**
     * If {@code details} is a block of colon-aligned key/value lines like
     * <pre>
     * ZDB ID          : ZDB-GENE-050419-154
     * Length          : 9069 -&gt; 9069
     * </pre>
     * convert it to a table. Lines containing {@code " -> "} produce
     * Before/After columns; otherwise just Field/Value. Returns null if any
     * non-blank line isn't k:v shaped — the caller should fall back to a
     * plain body.
     */
    static ReportTable tryParseKeyValueTable(String details) {
        if (details == null) return null;
        List<String[]> rows = new ArrayList<>();
        boolean anyArrow = false;
        for (String rawLine : details.split("\\R", -1)) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty()) continue;
            int colon = rawLine.indexOf(':');
            if (colon <= 0) return null;
            String key = rawLine.substring(0, colon).trim();
            String value = rawLine.substring(colon + 1).trim();
            if (key.isEmpty()) return null;
            String[] arrowSplit = value.split("\\s+->\\s+", 2);
            if (arrowSplit.length == 2) {
                anyArrow = true;
                rows.add(new String[]{key, arrowSplit[0], arrowSplit[1]});
            } else {
                rows.add(new String[]{key, value, null});
            }
            if (rows.size() > MAX_KV_LINES) return null;
        }
        if (rows.isEmpty()) return null;

        ReportTable table = new ReportTable();
        if (anyArrow) {
            table.addColumn(ReportTable.Column.of("field",  "Field"));
            table.addColumn(ReportTable.Column.of("before", "Before"));
            table.addColumn(ReportTable.Column.of("after",  "After"));
            for (String[] row : rows) {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("field",  row[0]);
                r.put("before", row[1]);
                r.put("after",  row[2] != null ? row[2] : "");
                table.addRow(r);
            }
        } else {
            table.addColumn(ReportTable.Column.of("field", "Field"));
            table.addColumn(ReportTable.Column.of("value", "Value"));
            for (String[] row : rows) {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("field", row[0]);
                r.put("value", row[1]);
                table.addRow(r);
            }
        }
        return table;
    }

    private static Map<String, Map<String, List<LoadReportAction>>> groupActions(List<LoadReportAction> actions) {
        Map<String, Map<String, List<LoadReportAction>>> result = new LinkedHashMap<>();
        if (actions == null) return result;
        for (LoadReportAction a : actions) {
            String type = a.getType() != null ? a.getType().name() : "INFO";
            String sub = a.getSubType() != null ? a.getSubType() : "(no subtype)";
            result.computeIfAbsent(type, k -> new LinkedHashMap<>())
                  .computeIfAbsent(sub, k -> new ArrayList<>())
                  .add(a);
        }
        return result;
    }

    private static String slugify(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
