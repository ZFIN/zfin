package org.zfin.datatransfer.go.service;

import org.zfin.datatransfer.go.GafErrorSummary;
import org.zfin.datatransfer.go.GafJobData;
import org.zfin.datatransfer.go.GafJobEntry;
import org.zfin.datatransfer.go.GafValidationError;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportTable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates {@link GafJobData} + {@link GafErrorSummary} into a generic
 * {@link Report} for the HTML viewer. Mirrors the information in the existing
 * _details.txt / _summary.txt / _error_summary.txt artifacts, except that the
 * EXISTING bucket is represented only by its count (the per-entry detail is
 * intentionally omitted — it's large and low-value).
 */
public class GafReportBuilder {

    private static final String CAT_ADDED    = "ADDED";
    private static final String CAT_UPDATED  = "UPDATED";
    private static final String CAT_REMOVED  = "REMOVED";
    private static final String CAT_ERROR    = "ERROR";
    private static final String CAT_INFO     = "INFO";

    private static final String SCHEMA_ANNOTATION  = "annotation";
    private static final String SCHEMA_ERROR_ENTRY = "errorEntry";
    private static final String SCHEMA_COUNT       = "labelCount";
    private static final String SCHEMA_KV          = "keyValue";

    /**
     * Pulls {@code key='value'} pairs out of toString() blobs like
     * {@code GafEntry{...}} or {@code MarkerGoTermEvidence{...}}.
     *
     * <p>Assumes the producer toString()s don't embed escaped apostrophes in
     * values (i.e. {@code name='O'Brien'} would terminate the value early and
     * drop the rest of the row's fields). True for the two source classes
     * today; if either ever quotes apostrophes in toString(), this needs a
     * tolerant tokenizer instead.
     */
    private static final Pattern KV_PATTERN = Pattern.compile("(\\w+)='([^']*)'");

    public Report build(String jobName,
                        String organization,
                        List<String> sourceUrls,
                        GafJobData data,
                        GafErrorSummary errorSummary) {
        return build(jobName, organization, sourceUrls, data, errorSummary, null, null);
    }

    public Report build(String jobName,
                        String organization,
                        List<String> sourceUrls,
                        GafJobData data,
                        GafErrorSummary errorSummary,
                        GoaLoadSnapshot before,
                        GoaLoadSnapshot after) {

        // No subtitle: the renderer already formats createdAt locale-aware
        // in the footer, so a server-locale Date.toString() above would be
        // redundant noise.
        Report report = new Report()
            .meta(new Report.Meta()
                .title(jobName + " — " + organization + " GAF Load")
                .createdAt(System.currentTimeMillis())
                .schemaVersion("1"))
            .definitions(buildDefinitions());

        ReportNode root = new ReportNode()
            .id("_root")
            .title(jobName)
            .body(Report.Body.text(
                "GAF/GOA load report for " + organization + ".\n\n" +
                "Use the navigation tree on the left to drill into added, updated, removed, " +
                "and errored annotations. Counts and a categorized error breakdown are below."));

        addSummaryTables(root, data, errorSummary, sourceUrls, organization, before, after);
        report.root(root);

        root.addChild(buildAdded(data));
        root.addChild(buildUpdated(data));
        root.addChild(buildRemoved(data));
        root.addChild(buildErrors(data, errorSummary));
        root.addChild(buildExisting(data));
        if (data.getRemappedMarkerIds() != null && !data.getRemappedMarkerIds().isEmpty()) {
            root.addChild(buildRemappedIds(data));
        }

        return report;
    }

    // -------- definitions --------

    private Report.Definitions buildDefinitions() {
        return new Report.Definitions()
            .field("marker",       Report.FieldDef.link("Gene", "https://zfin.org/{value}"))
            .field("markerName",   Report.FieldDef.text("Gene"))
            .field("zdbID",        Report.FieldDef.link("ZDB ID", "https://zfin.org/{value}"))
            .field("publication",  Report.FieldDef.link("Publication", "https://zfin.org/{value}"))
            .field("goTermID",     Report.FieldDef.link("GO term ID", "https://amigo.geneontology.org/amigo/term/{value}"))
            .field("uniprot",      Report.FieldDef.link("UniProt", "https://www.uniprot.org/uniprotkb/{value}"))
            .field("count",        Report.FieldDef.number("Count"))

            .tableSchema(SCHEMA_ANNOTATION, new Report.TableSchema()
                .description("Annotation entries (one row per MarkerGoTermEvidence).")
                .addColumn(ReportTable.Column.of("zdbID",        "ZDB ID",       "zdbID"))
                .addColumn(ReportTable.Column.of("marker",       "Gene"))
                .addColumn(ReportTable.Column.of("qualifier",    "Qualifier"))
                .addColumn(ReportTable.Column.of("goTerm",       "GO term"))
                .addColumn(ReportTable.Column.of("goTermID",     "GO ID",        "goTermID"))
                .addColumn(ReportTable.Column.of("evidence",     "Evidence"))
                .addColumn(ReportTable.Column.of("withFrom",     "With/From"))
                .addColumn(ReportTable.Column.of("annotExtn",    "Annotation extension"))
                .addColumn(ReportTable.Column.of("noctuaModel",  "Noctua model"))
                .addColumn(ReportTable.Column.of("source",       "Source pub",   "publication"))
                .addColumn(ReportTable.Column.of("organization", "Organization")))

            .tableSchema(SCHEMA_ERROR_ENTRY, new Report.TableSchema()
                .description("Errors with the GAF entry (or annotation) that triggered them.")
                .addColumn(ReportTable.Column.of("createdBy", "Created by"))
                .addColumn(ReportTable.Column.of("entryId",   "Identifier"))
                .addColumn(ReportTable.Column.of("qualifier", "Qualifier"))
                .addColumn(ReportTable.Column.of("goTerm",    "GO term"))
                .addColumn(ReportTable.Column.of("goTermID",  "GO ID",     "goTermID"))
                .addColumn(ReportTable.Column.of("evidence",  "Evidence"))
                .addColumn(ReportTable.Column.of("source",    "Source"))
                .addColumn(ReportTable.Column.of("message",   "Error")))

            .tableSchema(SCHEMA_COUNT, new Report.TableSchema()
                .addColumn(ReportTable.Column.of("label", "Category"))
                .addColumn(ReportTable.Column.of("count", "Count", "count")))

            .tableSchema(SCHEMA_KV, new Report.TableSchema()
                .addColumn(ReportTable.Column.of("key",   "Field"))
                .addColumn(ReportTable.Column.of("value", "Value")))

            .category(CAT_ADDED,   new Report.CategoryDef().label("Added").icon("➕").order(1)
                .description("Annotations newly added to the database in this load."))
            .category(CAT_UPDATED, new Report.CategoryDef().label("Updated").icon("✏️").order(2)
                .description("Annotations that already existed but were updated by this load."))
            .category(CAT_REMOVED, new Report.CategoryDef().label("Removed").icon("🗑").order(3)
                .description("Previously-loaded annotations that are no longer present in the new GAF file."))
            .category(CAT_ERROR,   new Report.CategoryDef().label("Errors").icon("❌").order(4)
                .description("Validation, parsing, and database errors encountered during the load."))
            .category(CAT_INFO,    new Report.CategoryDef().label("Info").icon("ℹ️").order(5)
                .description("Informational notes; nothing was acted on."));
    }

    // -------- summary tables (root) --------

    private void addSummaryTables(ReportNode root, GafJobData data, GafErrorSummary errorSummary,
                                  List<String> sourceUrls, String organization,
                                  GoaLoadSnapshot before, GoaLoadSnapshot after) {
        ReportTable counts = new ReportTable()
            .schemaRef(SCHEMA_COUNT)
            .title("Action breakdown")
            .addRow("label", "Added",    "count", data.getNewEntries().size())
            .addRow("label", "Updated",  "count", data.getUpdateEntries().size())
            .addRow("label", "Removed",  "count", data.getRemovedEntries().size())
            .addRow("label", "Errors",   "count", data.getErrors().size())
            .addRow("label", "Existing (skipped, no detail)", "count", data.getExistingEntries().size())
            .addRow("label", "IDs corrected (merged/retyped)", "count", data.getRemappedMarkerIds().size());
        root.addTable(counts);

        ReportTable parserStats = new ReportTable()
            .schemaRef(SCHEMA_KV)
            .title("Parser stats")
            .addRow("key", "Total GAF entries parsed",         "value", data.getGafEntryCount())
            .addRow("key", "Inferences containing pipes",      "value", data.getInfPipeCount())
            .addRow("key", "Inferences containing commas",     "value", data.getInfCommaCount())
            .addRow("key", "Inferences with both pipes/commas","value", data.getInfBothCount());
        root.addTable(parserStats);

        Map<String, Integer> rejections = errorSummary == null ? null : errorSummary.getParserRejections();
        if (rejections != null && !rejections.isEmpty()) {
            ReportTable t = new ReportTable()
                .schemaRef(SCHEMA_COUNT)
                .title("Entries filtered during parsing");
            rejections.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> t.addRow("label", e.getKey(), "count", e.getValue()));
            root.addTable(t);
        }

        appendLoadImpactTables(root, organization, before, after);

        if (sourceUrls != null && !sourceUrls.isEmpty()) {
            for (String url : sourceUrls) {
                root.addLink("Source: " + url, url);
            }
        }
    }

    // -------- per-category nodes --------

    private ReportNode buildAdded(GafJobData data) {
        ReportNode node = new ReportNode()
            .id("cat-added")
            .title("Added")
            .categoryRef(CAT_ADDED)
            .count(data.getNewEntries().size())
            .body(Report.Body.text("New annotations being inserted into the database."));
        ReportTable table = newAnnotationTable("Added annotations (" + data.getNewEntries().size() + ")");
        appendAnnotations(table, data.getNewEntries());
        if (table.getRows() != null) node.addTable(table);
        return node;
    }

    private ReportNode buildUpdated(GafJobData data) {
        ReportNode node = new ReportNode()
            .id("cat-updated")
            .title("Updated")
            .categoryRef(CAT_UPDATED)
            .count(data.getUpdateEntries().size())
            .body(Report.Body.text("Existing annotations whose fields were updated by this load."));
        ReportTable table = newAnnotationTable("Updated annotations (" + data.getUpdateEntries().size() + ")");
        appendAnnotations(table, data.getUpdateEntries());
        if (table.getRows() != null) node.addTable(table);
        return node;
    }

    private ReportNode buildRemoved(GafJobData data) {
        ReportNode node = new ReportNode()
            .id("cat-removed")
            .title("Removed")
            .categoryRef(CAT_REMOVED)
            .count(data.getRemovedEntries().size())
            .body(Report.Body.text("Annotations that were in ZFIN before this load but are not in the new GAF file."));
        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_ANNOTATION)
            .title("Removed annotations (" + data.getRemovedEntries().size() + ")");
        for (GafJobEntry e : data.getRemovedEntries()) {
            table.addRow(
                "zdbID",        safe(e.getZdbID()),
                "marker",       safe(e.getMarker()),
                "qualifier",    safe(e.getQualifierRelation()),
                "goTerm",       safe(e.getGoTermName()),
                "goTermID",     safe(e.getGoTermID()),
                "evidence",     safe(e.getEvidenceCode()),
                "withFrom",     safe(e.getWithFrom()),
                "annotExtn",    safe(e.getAnnotationExtensions()),
                "noctuaModel",  safe(e.getNoctuaModelId()),
                "source",       safe(e.getSource()),
                "organization", safe(e.getOrganizationCreatedBy())
            );
        }
        if (table.getRows() != null) node.addTable(table);
        return node;
    }

    private ReportNode buildErrors(GafJobData data, GafErrorSummary errorSummary) {
        ReportNode node = new ReportNode()
            .id("cat-errors")
            .title("Errors")
            .categoryRef(CAT_ERROR)
            .count(data.getErrors().size())
            .body(Report.Body.text(
                "Validation, parsing, and database errors encountered during the load. " +
                "Use the All errors table below to filter across categories (e.g. type " +
                "\"ZFIN\" to find errors on ZFIN-curated annotations), or click into a " +
                "category for examples and detail."));

        if (errorSummary == null) return node;

        Map<String, Integer> categoryCounts = errorSummary.getCategoryCounts();
        if (categoryCounts != null && !categoryCounts.isEmpty()) {
            ReportTable summary = new ReportTable()
                .schemaRef(SCHEMA_COUNT)
                .title("Error counts by category");
            categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> summary.addRow("label", e.getKey(), "count", e.getValue()));
            node.addTable(summary);

            node.addTable(buildAllErrorsTable(data.getErrors()));

            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                node.addChild(buildErrorCategoryNode(entry.getKey(), entry.getValue(), data, errorSummary));
            }
        }

        return node;
    }

    /**
     * Flat list of every error with a Category column up front. The viewer's
     * per-table filter input then lets a curator narrow by Created by (e.g.
     * "ZFIN") across categories — useful because many errors come from
     * annotations curated by other organizations and aren't ZFIN's to fix.
     */
    private ReportTable buildAllErrorsTable(List<GafValidationError> errors) {
        ReportTable table = new ReportTable()
            .title("All errors (" + errors.size() + ")")
            .description("Filter by any column (e.g. \"ZFIN\" in Created by).")
            .addColumn(ReportTable.Column.of("category",  "Category"))
            .addColumn(ReportTable.Column.of("createdBy", "Created by"))
            .addColumn(ReportTable.Column.of("entryId",   "Identifier"))
            .addColumn(ReportTable.Column.of("qualifier", "Qualifier"))
            .addColumn(ReportTable.Column.of("goTerm",    "GO term"))
            .addColumn(ReportTable.Column.of("goTermID",  "GO ID",     "goTermID"))
            .addColumn(ReportTable.Column.of("evidence",  "Evidence"))
            .addColumn(ReportTable.Column.of("source",    "Source",    "publication"))
            .addColumn(ReportTable.Column.of("message",   "Error"));
        for (GafValidationError err : errors) {
            String msg = err.getMessage();
            if (msg == null) continue;
            String firstLine = msg.split("\n", 2)[0].strip();
            Map<String, Object> row = buildErrorRow(err, firstLine);
            row.put("category", GafErrorSummary.categorize(firstLine));
            table.addRow(row);
        }
        return table;
    }

    private ReportNode buildErrorCategoryNode(String category, int count,
                                              GafJobData data, GafErrorSummary errorSummary) {
        ReportNode child = new ReportNode()
            .id("err-" + slugify(category))
            .title(category)
            .categoryRef(CAT_ERROR)
            .count(count)
            .body(Report.Body.text(count + " error" + (count == 1 ? "" : "s")
                + " in this category. The matching errors are listed below."));

        if ("Gene not found for ID".equals(category)) {
            addGeneNotFoundDetail(child, errorSummary);
        }

        // Attach matching error messages, parsed into structured columns.
        // Use GafErrorSummary.categorize() as the single source of truth so the
        // matching logic can't drift from how the categories are bucketed.
        ReportTable matches = new ReportTable()
            .schemaRef(SCHEMA_ERROR_ENTRY)
            .title("Matching errors");
        for (GafValidationError err : data.getErrors()) {
            String msg = err.getMessage();
            if (msg == null) continue;
            String firstLine = msg.split("\n", 2)[0].strip();
            if (!category.equals(GafErrorSummary.categorize(firstLine))) continue;
            matches.addRow(buildErrorRow(err, firstLine));
        }
        if (matches.getRows() != null) child.addTable(matches);
        return child;
    }

    /**
     * Parses one error message into the column shape shared by the per-category
     * "Matching errors" table and the flat "All errors" table.
     *
     * <p>GafEntry shape carries {@code goid} (the OBO ID); MarkerGoTermEvidence
     * carries {@code goTerm} (the name). Keep them in their respective columns
     * so the OBO link only renders for real GO IDs.
     */
    private static Map<String, Object> buildErrorRow(GafValidationError err, String firstLine) {
        Map<String, String> f = parseEntryFields(err.getMessage());
        String goid = f.getOrDefault("goid", "");
        String goName = goid.isEmpty() ? f.getOrDefault("goTerm", "") : "";
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("entryId",   firstNonEmpty(f.get("entryId"), f.get("zdbID")));
        row.put("qualifier", firstNonEmpty(f.get("qualifier"), f.get("qualifierRelation")));
        row.put("goTerm",    goName);
        row.put("goTermID",  goid);
        row.put("evidence",  firstNonEmpty(f.get("evidenceCode")));
        row.put("source",    firstNonEmpty(f.get("pmid"), f.get("source")));
        row.put("createdBy", firstNonEmpty(f.get("createdBy"), f.get("organizationCreatedBy")));
        row.put("message",   firstLine);
        return row;
    }

    /** Extracts key='value' pairs from the GafEntry{...} or MarkerGoTermEvidence{...} blob inside a message. */
    static Map<String, String> parseEntryFields(String message) {
        Map<String, String> out = new LinkedHashMap<>();
        int gafIdx  = message.indexOf("GafEntry{");
        int mgteIdx = message.indexOf("MarkerGoTermEvidence{");
        int start = -1;
        if (gafIdx  >= 0) start = gafIdx  + "GafEntry{".length();
        else if (mgteIdx >= 0) start = mgteIdx + "MarkerGoTermEvidence{".length();
        if (start < 0) return out;
        String inner = message.substring(start);
        int end = inner.lastIndexOf('}');
        if (end >= 0) inner = inner.substring(0, end);
        Matcher m = KV_PATTERN.matcher(inner);
        while (m.find()) {
            out.put(m.group(1), m.group(2));
        }
        return out;
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isEmpty()) return v;
        }
        return "";
    }

    private void addGeneNotFoundDetail(ReportNode node, GafErrorSummary errorSummary) {
        Map<String, Integer> ids = errorSummary.getGeneNotFoundIds();
        Map<String, Integer> sources = errorSummary.getGeneNotFoundSources();
        if (ids == null || ids.isEmpty()) return;

        int uniqueIds = ids.size();
        int totalOccurrences = ids.values().stream().mapToInt(Integer::intValue).sum();

        java.util.Map<String, Integer> idTypeUnique = new java.util.LinkedHashMap<>();
        for (String id : ids.keySet()) {
            idTypeUnique.merge(GafErrorSummary.idType(id), 1, Integer::sum);
        }

        ReportTable typeTable = new ReportTable()
            .schemaRef(SCHEMA_COUNT)
            .title("By ID type (unique IDs: " + uniqueIds + ")");
        idTypeUnique.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> typeTable.addRow("label", e.getKey(), "count", e.getValue()));
        node.addTable(typeTable);

        if (sources != null && !sources.isEmpty()) {
            ReportTable srcTable = new ReportTable()
                .schemaRef(SCHEMA_COUNT)
                .title("By annotation source (occurrences: " + totalOccurrences + ")");
            sources.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> srcTable.addRow("label", e.getKey(), "count", e.getValue()));
            node.addTable(srcTable);
        }

        ReportTable idTable = new ReportTable()
            .title("Top IDs by occurrence")
            .addColumn(ReportTable.Column.of("id",    "Identifier"))
            .addColumn(ReportTable.Column.of("type",  "Type"))
            .addColumn(ReportTable.Column.of("count", "Occurrences", "count"));
        ids.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(50)
            .forEach(e -> idTable.addRow(
                "id", e.getKey(),
                "type", GafErrorSummary.idType(e.getKey()),
                "count", e.getValue()));
        node.addTable(idTable);
    }

    /**
     * Append the three "Load impact" tables (overall / per evidence code /
     * per source pub) inline on the given node — typically the root summary —
     * showing MGOE counts attributed to the organization, measured immediately
     * before and after the load. No-op for non-GOA loads or when either
     * snapshot is missing (e.g. a snapshot query failed mid-load).
     */
    private void appendLoadImpactTables(ReportNode node, String organization,
                                        GoaLoadSnapshot before, GoaLoadSnapshot after) {
        if (before == null || after == null) return;
        node.addTable(buildOverallImpactTable(before, after));
        node.addTable(buildEvidenceCodeImpactTable(before, after));
        node.addTable(buildSourceImpactTable(before, after));
    }

    private ReportTable buildOverallImpactTable(GoaLoadSnapshot before, GoaLoadSnapshot after) {
        ReportTable table = newImpactTable("Totals", "label", "Metric");
        addImpactRow(table, "Total annotations",
            before.getTotalAnnotations(), after.getTotalAnnotations());
        addImpactRow(table, "Distinct GO terms used",
            before.getDistinctGoTerms(), after.getDistinctGoTerms());
        addImpactRow(table, "Markers with annotation",
            before.getDistinctMarkers(), after.getDistinctMarkers());
        return table;
    }

    private ReportTable buildEvidenceCodeImpactTable(GoaLoadSnapshot before, GoaLoadSnapshot after) {
        ReportTable table = newImpactTable(
            "Annotations per evidence code",
            "label", "Evidence code");
        for (String code : unionKeys(before.getCountsByEvidenceCode(), after.getCountsByEvidenceCode())) {
            addImpactRow(table, code,
                before.getCountsByEvidenceCode().getOrDefault(code, 0L),
                after.getCountsByEvidenceCode().getOrDefault(code, 0L));
        }
        return table;
    }

    private ReportTable buildSourceImpactTable(GoaLoadSnapshot before, GoaLoadSnapshot after) {
        // Source is a publication ZDB ID — render it as a ZFIN link via the
        // shared "publication" field def so curators can click through. Rows
        // whose net change is zero are dropped: most source pubs don't move
        // each load and the unchanged rows drown out the interesting ones.
        ReportTable table = new ReportTable()
            .title("Annotations per source publication (excluding unchanged)")
            .description("Before/after counts grouped by source publication; unchanged rows omitted.")
            .addColumn(ReportTable.Column.of("label",   "Publication", "publication"))
            .addColumn(ReportTable.Column.of("before",  "# Before",    "count"))
            .addColumn(ReportTable.Column.of("after",   "# After",     "count"))
            .addColumn(ReportTable.Column.of("change",  "Net change",  "count"))
            .addColumn(ReportTable.Column.of("percent", "% change"));
        for (String src : unionKeys(before.getCountsBySource(), after.getCountsBySource())) {
            long b = before.getCountsBySource().getOrDefault(src, 0L);
            long a = after.getCountsBySource().getOrDefault(src, 0L);
            if (a == b) continue;
            addImpactRow(table, src, b, a);
        }
        return table;
    }

    private ReportTable newImpactTable(String title, String labelKey, String labelHeader) {
        return new ReportTable()
            .title(title)
            .addColumn(ReportTable.Column.of(labelKey, labelHeader))
            .addColumn(ReportTable.Column.of("before",  "# Before",   "count"))
            .addColumn(ReportTable.Column.of("after",   "# After",    "count"))
            .addColumn(ReportTable.Column.of("change",  "Net change", "count"))
            .addColumn(ReportTable.Column.of("percent", "% change"));
    }

    private static void addImpactRow(ReportTable table, String label, long before, long after) {
        long change = after - before;
        // Skip rows that are zero on both sides — usually means the category
        // didn't exist in either snapshot and adds no signal.
        if (before == 0 && after == 0) return;
        String percent = before == 0
            ? (after == 0 ? "" : "—")
            : String.format("%+.2f%%", (change * 100.0) / before);
        table.addRow(
            "label",   label,
            "before",  before,
            "after",   after,
            "change",  change,
            "percent", percent);
    }

    /** Ordered union of keys: present-in-both first by descending after-count, then before-only. */
    private static Set<String> unionKeys(Map<String, Long> before, Map<String, Long> after) {
        Set<String> keys = new LinkedHashSet<>(after.keySet());
        keys.addAll(before.keySet());
        return keys;
    }

    private ReportNode buildExisting(GafJobData data) {
        int n = data.getExistingEntries().size();
        return new ReportNode()
            .id("cat-existing")
            .title("Existing")
            .categoryRef(CAT_INFO)
            .count(n)
            .body(Report.Body.text(
                n + " annotation" + (n == 1 ? "" : "s")
                + " already existed in the database and were skipped. " +
                "Per-entry detail is omitted to keep this report small; consult " +
                "_details.txt for the full list."));
    }

    /**
     * Merged/retyped ZFIN object ids that were corrected against zdb_replaced_data on import,
     * so curators can see which annotations were remapped to a surviving marker rather than
     * silently loaded under a new id.
     */
    private ReportNode buildRemappedIds(GafJobData data) {
        List<String[]> remaps = data.getRemappedMarkerIds();
        ReportNode node = new ReportNode()
            .id("cat-remapped")
            .title("IDs corrected (merged/retyped)")
            .categoryRef(CAT_INFO)
            .count(remaps.size())
            .body(Report.Body.text(
                remaps.size() + " object id" + (remaps.size() == 1 ? "" : "s")
                + " in the file were merged or type-changed in ZFIN after upstream curation; "
                + "each was remapped to its current marker via zdb_replaced_data before load."));
        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_KV)
            .title("Old id → current marker");
        for (String[] r : remaps) {
            String newLabel = (r.length > 2 && r[2] != null && !r[2].isEmpty())
                ? r[2] + " (" + r[1] + ")" : r[1];
            table.addRow("key", r[0], "value", newLabel);
        }
        node.addTable(table);
        return node;
    }

    // -------- helpers --------

    private ReportTable newAnnotationTable(String title) {
        return new ReportTable().schemaRef(SCHEMA_ANNOTATION).title(title);
    }

    private void appendAnnotations(ReportTable table, Collection<MarkerGoTermEvidence> entries) {
        for (MarkerGoTermEvidence e : entries) {
            table.addRow(
                "zdbID",        safe(e.getZdbID()),
                "marker",       e.getMarker() != null ? safe(e.getMarker().getAbbreviation()) : "",
                "qualifier",    e.getQualifierRelation() != null ? safe(e.getQualifierRelation().getTermName()) : "",
                "goTerm",       e.getGoTerm() != null ? safe(e.getGoTerm().getTermName()) : "",
                "goTermID",     e.getGoTerm() != null ? safe(e.getGoTerm().getOboID()) : "",
                "evidence",     e.getEvidenceCode() != null ? safe(e.getEvidenceCode().getName()) : "",
                "withFrom",     GafJobEntry.formatWithFrom(e),
                "annotExtn",    GafJobEntry.formatAnnotationExtensions(e),
                "noctuaModel",  safe(e.getNoctuaModelId()),
                "source",       e.getSource() != null ? safe(e.getSource().getZdbID()) : "",
                "organization", safe(e.getOrganizationCreatedBy())
            );
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String slugify(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
