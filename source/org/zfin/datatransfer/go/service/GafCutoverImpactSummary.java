package org.zfin.datatransfer.go.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportReader;
import org.zfin.report.ReportTable;
import org.zfin.report.ReportWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Adds a "Cutover Impact Summary" node to an already-generated GafLoadJob HTML report --
 * per-org before/after row counts, plus the true_loss/subsumed/specificity_lost breakdown from
 * mgte_subsumption.xlsx -- as a sibling of the existing top-level nodes (Added, Updated, Removed,
 * Errors, Existing, IDs corrected), reachable from the left-nav tree the same way they are.
 *
 * <p>Why a post-processing step rather than built into {@link GafReportBuilder}: the data it
 * needs (the AFTER snapshot, the per-org diff, the subsumption workbook) is produced by shell
 * steps in the Jenkins job that run AFTER the Java load and its HTML report are already written
 * -- see RUNBOOK-danre-mod-load.md §13. There is no point in the load's own process where this
 * information exists yet.
 *
 * <p>Reads the report back with {@link ReportReader}, adds a new {@link ReportNode} as the first
 * child of root (with its own category so it sorts first in the tree), and re-renders the whole
 * file with {@link ReportWriter} -- rather than the earlier approach of splicing a static HTML
 * block above the viewer, which had no entry in the left-nav and didn't look or behave like the
 * rest of the report.
 *
 * <p>Idempotent: re-running replaces a previous injection (matched by node id) rather than
 * duplicating it. Missing inputs (e.g. a QC run with no subsumption workbook because
 * RUN_CUTOVER_SCRIPTS was off, or mgte_subsumption.sh failed) degrade to a node saying so, not to
 * failing the build -- this summary is a convenience on top of the real artifacts, never a gate
 * on them.
 *
 * <p>Usage: {@code gradle gafCutoverImpactSummary --args="<report.html> <dbdiff-dir>"}.
 */
public class GafCutoverImpactSummary {

    private static final String NODE_ID = "cat-cutover-impact";
    private static final String CAT_IMPACT = "IMPACT";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: GafCutoverImpactSummary <report.html> <dbdiff-dir>");
            System.exit(1);
        }
        File htmlFile = new File(args[0]);
        File dbdiffDir = new File(args[1]);
        try {
            new GafCutoverImpactSummary().run(htmlFile, dbdiffDir);
        } catch (Exception e) {
            // Never fail the build over this -- see class javadoc.
            System.err.println("GafCutoverImpactSummary: failed to inject impact summary: " + e);
            e.printStackTrace();
        }
    }

    public void run(File htmlFile, File dbdiffDir) throws IOException {
        if (!htmlFile.isFile()) {
            System.err.println("GafCutoverImpactSummary: no such report file: " + htmlFile);
            return;
        }

        Report report = new ReportReader().read(htmlFile.toPath());
        ReportNode root = report.getRoot();
        if (root == null) {
            System.err.println("GafCutoverImpactSummary: report has no root node, skipping: " + htmlFile);
            return;
        }

        ReportNode impactNode = new ReportNode()
            .id(NODE_ID)
            .title("Cutover Impact Summary")
            .categoryRef(CAT_IMPACT)
            .body(Report.Body.text(
                "Per-org row counts and the true_loss / subsumed / specificity_lost breakdown "
                + "from this run's snapshot/diff/subsumption pipeline -- see the description on "
                + "each table below for what it shows."));

        boolean any = false;
        any |= addOrgCountsTable(impactNode, dbdiffDir);
        any |= addSheetTable(impactNode, dbdiffDir, "mgte_subsumption_by_bucket.csv",
            "Lost (gene, GO) pairs by bucket",
            "subsumed = the gene retains a more specific descendant term (not a loss). "
            + "specificity_lost = only a broader ancestor remains (partial loss). "
            + "true_loss = nothing in that lineage survives on that gene.");
        any |= addSheetTable(impactNode, dbdiffDir, "mgte_subsumption_by_org.csv",
            "True loss by owning organization (as held before this run)", null);
        any |= addSheetTable(impactNode, dbdiffDir, "mgte_subsumption_by_org_source.csv",
            "True loss by organization and source publication",
            "Breaks each organization's true loss down by the publication that asserted it -- "
            + "e.g. how much of UniProt's loss is kw2go vs. interpro2go vs. ec2go, or how much "
            + "of Noctua's is curator-assigned ND (\"unknown\") vs. experimental.");

        if (!any) {
            impactNode.body(Report.Body.text(
                "No before/after or subsumption data found under " + dbdiffDir
                + " -- this section is only populated when the job's snapshot/diff/subsumption "
                + "steps have run."));
        }

        ensureImpactCategoryDefined(report);
        replaceExistingChild(root, NODE_ID, impactNode);
        enrichRemovedWithLossType(report, dbdiffDir);

        new ReportWriter().write(report, htmlFile);
        System.out.println("GafCutoverImpactSummary: added impact summary node to " + htmlFile);
    }

    /** Register the category the new node refers to, if this report's Definitions don't have it yet. */
    private static void ensureImpactCategoryDefined(Report report) {
        if (report.getDefinitions() == null) {
            report.definitions(new Report.Definitions());
        }
        Map<String, Report.CategoryDef> categories = report.getDefinitions().getCategories();
        if (categories != null && categories.containsKey(CAT_IMPACT)) {
            return;
        }
        report.getDefinitions().category(CAT_IMPACT, new Report.CategoryDef()
            .label("Impact Summary")
            .icon("📊")
            .order(0) // sorts before Added/Updated/Removed/Errors/Existing (order 1-5)
            .description("Per-org before/after counts and the true-loss / subsumed / "
                + "specificity-lost breakdown for this run."));
    }

    /** Insert asNode as the first child, removing any existing child with the same id first. */
    private static void replaceExistingChild(ReportNode root, String id, ReportNode asNode) {
        List<ReportNode> children = root.getChildren();
        if (children == null) {
            root.addChild(asNode);
            return;
        }
        children.removeIf(c -> id.equals(c.getId()));
        children.add(0, asNode);
    }

    /**
     * Adds a "Loss type" column to the Removed table (GafReportBuilder's {@code cat-removed}
     * node), populated from the subsumption detail sheets, keyed on (gene, GO id) -- the same
     * join key mgte_subsumption.sql itself uses. A removed row whose pair isn't in any bucket
     * simply isn't lost at the (gene, GO) level (e.g. the phylo GOA -> PAINT re-home: the GOA row
     * is "removed" but the same pair exists again under PAINT), so it's left blank rather than
     * guessed at.
     *
     * <p>Requires the SAME dbdiffDir the impact tables above read from, so if those are missing
     * (no subsumption workbook) this silently leaves the Removed table exactly as
     * GafReportBuilder wrote it -- adding a column of blanks would be worse than not adding one.
     */
    private void enrichRemovedWithLossType(Report report, File dbdiffDir) {
        ReportNode removedNode = findChild(report.getRoot(), "cat-removed");
        if (removedNode == null || removedNode.getTables() == null || removedNode.getTables().isEmpty()) {
            return;
        }
        ReportTable removedTable = removedNode.getTables().get(0);
        if (removedTable.getRows() == null || removedTable.getRows().isEmpty()) {
            return;
        }

        Map<String, String> lossTypeByKey = loadLossTypeLookup(dbdiffDir);
        if (lossTypeByKey.isEmpty()) {
            return;
        }

        // This table normally carries no inline columns of its own -- GafReportBuilder gives it
        // schemaRef(SCHEMA_ANNOTATION) and leaves the viewer to resolve columns from that shared
        // schema. Calling addColumn() here without first copying the schema's own columns across
        // would REPLACE that resolution rather than extend it: report-template.html's
        // `columns = table.columns || schema.columns` only falls back to the schema when
        // table.columns is still null, and one addColumn() call ends that. The visible symptom
        // was every other column (zdbID, marker, goTerm, ...) silently vanishing the moment this
        // ran -- caught before this shipped, not after.
        List<ReportTable.Column> existingColumns = effectiveColumns(report, removedTable);
        boolean hasLossTypeColumn = existingColumns.stream().anyMatch(c -> "lossType".equals(c.getKey()));
        if (!hasLossTypeColumn) {
            // Same reasoning as the columns above: the table has no inline description of its
            // own (falls back to the schema's), so setting one here without first carrying the
            // schema's across would silently replace it rather than extend it. Gated on the same
            // hasLossTypeColumn check as the columns, so a re-run (idempotency) doesn't
            // re-append this note to itself.
            String schemaDescription = effectiveDescription(report, removedTable);
            String note = "\"Loss type\" (from this run's subsumption analysis): blank means "
                + "this (gene, GO term) pair is still asserted some other way after this run -- a "
                + "different organization/source now supplies it, so it was never actually lost "
                + "at the (gene, GO) level (e.g. the phylo GOA -> PAINT re-home). Otherwise: "
                + "true_loss (nothing in that lineage survives on the gene), subsumed (a more "
                + "specific descendant term does -- not a real loss), or specificity_lost (only a "
                + "broader ancestor does). See the Cutover Impact Summary for the totals.";
            removedTable.description(schemaDescription == null || schemaDescription.isBlank()
                ? note : schemaDescription + " " + note);

            for (ReportTable.Column c : existingColumns) {
                removedTable.addColumn(c);
            }
            removedTable.addColumn(ReportTable.Column.of("lossType", "Loss type"));
        }
        for (Map<String, Object> row : removedTable.getRows()) {
            String key = lossTypeKey(String.valueOf(row.get("marker")), String.valueOf(row.get("goTermID")));
            row.put("lossType", lossTypeByKey.getOrDefault(key, ""));
        }
    }

    /** A table's own columns if it has any, else its schemaRef's columns, else empty. */
    private static List<ReportTable.Column> effectiveColumns(Report report, ReportTable table) {
        if (table.getColumns() != null) {
            return table.getColumns();
        }
        Report.TableSchema schema = resolveSchema(report, table);
        return schema != null && schema.getColumns() != null ? schema.getColumns() : List.of();
    }

    /** A table's own description if it has one, else its schemaRef's description, else null. */
    private static String effectiveDescription(Report report, ReportTable table) {
        if (table.getDescription() != null) {
            return table.getDescription();
        }
        Report.TableSchema schema = resolveSchema(report, table);
        return schema != null ? schema.getDescription() : null;
    }

    private static Report.TableSchema resolveSchema(Report report, ReportTable table) {
        String schemaRef = table.getSchemaRef();
        if (schemaRef == null || report.getDefinitions() == null
            || report.getDefinitions().getTableSchemas() == null) {
            return null;
        }
        return report.getDefinitions().getTableSchemas().get(schemaRef);
    }

    private static ReportNode findChild(ReportNode parent, String id) {
        if (parent == null || parent.getChildren() == null) {
            return null;
        }
        for (ReportNode c : parent.getChildren()) {
            if (id.equals(c.getId())) {
                return c;
            }
        }
        return null;
    }

    private static String lossTypeKey(String gene, String goId) {
        return gene + "\u0001" + goId;
    }

    /**
     * Reads mgte_subsumption.sql's three per-pair detail sheets (true_loss / subsumed /
     * specificity_lost) out of the workbook -- these are the ONE row per (gene, GO term) pair
     * detail sheets, not the by_bucket/by_org/by_org_source summaries the tables above use -- and
     * returns a (gene, GO id) -> bucket lookup covering every pair this run lost.
     */
    private Map<String, String> loadLossTypeLookup(File dbdiffDir) {
        Map<String, String> lookup = new LinkedHashMap<>();
        File workbookFile = new File(dbdiffDir, "mgte_subsumption.xlsx");
        if (!workbookFile.isFile()) {
            return lookup;
        }
        try (var in = java.nio.file.Files.newInputStream(workbookFile.toPath())) {
            Workbook workbook = WorkbookFactory.create(in);
            for (String csvName : List.of("mgte_subsumption_true_loss.csv",
                                           "mgte_subsumption_subsumed.csv",
                                           "mgte_subsumption_specificity_lost.csv")) {
                // Same truncation CSVToXLSXConverter applies when naming sheets -- see
                // addSheetTableFromWorkbook.
                String sheetName = csvName.substring(0, csvName.length() - 4);
                sheetName = sheetName.substring(0, Math.min(sheetName.length(), 30));
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    continue;
                }
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    continue;
                }
                List<String> headers = new ArrayList<>();
                for (Cell cell : headerRow) headers.add(cellText(cell));
                int bucketIdx = headers.indexOf("bucket");
                int geneIdx = headers.indexOf("gene");
                int goIdIdx = headers.indexOf("go_id");
                if (bucketIdx < 0 || geneIdx < 0 || goIdIdx < 0) {
                    System.err.println("GafCutoverImpactSummary: sheet " + sheetName
                        + " is missing an expected column (bucket/gene/go_id), skipping");
                    continue;
                }
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    String bucket = cellText(row.getCell(bucketIdx));
                    String gene = cellText(row.getCell(geneIdx));
                    String goId = cellText(row.getCell(goIdIdx));
                    lookup.put(lossTypeKey(gene, goId), bucket);
                }
            }
        } catch (Exception e) {
            System.err.println("GafCutoverImpactSummary: could not read subsumption detail sheets "
                + "from " + workbookFile + ": " + e);
        }
        return lookup;
    }

    /** Per-org row counts, before vs. after, from the --all snapshot pair every run produces. */
    private boolean addOrgCountsTable(ReportNode node, File dbdiffDir) {
        File beforeFile = new File(dbdiffDir, "mgte_before_ALL.csv");
        File afterFile = new File(dbdiffDir, "mgte_after_ALL.csv");
        if (!beforeFile.isFile() || !afterFile.isFile()) {
            return false;
        }
        try {
            Map<String, Integer> before = countByOrg(beforeFile);
            Map<String, Integer> after = countByOrg(afterFile);

            Map<String, Integer> allOrgs = new LinkedHashMap<>();
            before.forEach((org, count) -> allOrgs.put(org, 0));
            after.forEach((org, count) -> allOrgs.putIfAbsent(org, 0));

            ReportTable table = new ReportTable()
                .title("Per-org row counts, before → after")
                .addColumn(ReportTable.Column.of("org", "Org"))
                .addColumn(ReportTable.Column.of("before", "Before"))
                .addColumn(ReportTable.Column.of("after", "After"))
                .addColumn(ReportTable.Column.of("net", "Net"));
            for (String org : allOrgs.keySet()) {
                int b = before.getOrDefault(org, 0);
                int a = after.getOrDefault(org, 0);
                int net = a - b;
                table.addRow("org", org, "before", b, "after", a,
                    "net", (net > 0 ? "+" : "") + net);
            }
            node.addTable(table);
            return true;
        } catch (IOException e) {
            System.err.println("GafCutoverImpactSummary: could not read org counts: " + e);
            return false;
        }
    }

    private static Map<String, Integer> countByOrg(File csvFile) throws IOException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (Reader reader = new FileReader(csvFile);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            for (CSVRecord record : parser) {
                counts.merge(record.get("org"), 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * Renders one of mgte_subsumption.sql's summary sheets as a table. Tries the CSV first (only
     * present if something left it lying around -- the normal pipeline converts it to the
     * workbook and deletes it), then falls back to reading the same-named sheet out of
     * mgte_subsumption.xlsx, which is what survives in the real pipeline.
     */
    private boolean addSheetTable(ReportNode node, File dbdiffDir, String csvName,
                                   String title, String note) {
        File csvFile = new File(dbdiffDir, csvName);
        if (csvFile.isFile()) {
            try (Reader reader = new FileReader(csvFile);
                 CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
                List<String> headers = parser.getHeaderNames();
                List<List<String>> rows = new ArrayList<>();
                for (CSVRecord record : parser) {
                    List<String> values = new ArrayList<>(record.size());
                    for (String v : record) values.add(v);
                    rows.add(values);
                }
                node.addTable(buildTable(title, note, headers, rows));
                return true;
            } catch (IOException e) {
                System.err.println("GafCutoverImpactSummary: could not read " + csvName + ": " + e);
                return false;
            }
        }
        return addSheetTableFromWorkbook(node, dbdiffDir, csvName, title, note);
    }

    private boolean addSheetTableFromWorkbook(ReportNode node, File dbdiffDir, String csvName,
                                               String title, String note) {
        File workbookFile = new File(dbdiffDir, "mgte_subsumption.xlsx");
        if (!workbookFile.isFile()) {
            return false;
        }
        // mgte_subsumption_by_bucket.csv -> sheet "mgte_subsumption_by_bucket" (CSVToXLSXConverter
        // names each sheet after the source CSV's filename, minus the extension).
        String sheetName = csvName.endsWith(".csv") ? csvName.substring(0, csvName.length() - 4) : csvName;
        sheetName = sheetName.substring(0, Math.min(sheetName.length(), 30));
        try (var in = java.nio.file.Files.newInputStream(workbookFile.toPath())) {
            Workbook workbook = WorkbookFactory.create(in);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return false;
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return false;
            }
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) headers.add(cellText(cell));

            List<List<String>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                List<String> values = new ArrayList<>();
                for (int c = 0; c < headers.size(); c++) {
                    values.add(cellText(row.getCell(c)));
                }
                rows.add(values);
            }
            node.addTable(buildTable(title, note, headers, rows));
            return true;
        } catch (Exception e) {
            System.err.println("GafCutoverImpactSummary: could not read sheet " + sheetName
                + " from " + workbookFile + ": " + e);
            return false;
        }
    }

    private static ReportTable buildTable(String title, String note,
                                           List<String> headers, List<List<String>> rows) {
        ReportTable table = new ReportTable().title(title);
        if (note != null) {
            table.description(note);
        }
        // Dedup keys defensively: two sheet columns sharing a lowercased header would otherwise
        // collide in the row map. Not expected from these known sheets, but this table's shape
        // is driven by whatever columns the SQL happens to select, not a fixed schema.
        List<String> keys = new ArrayList<>();
        var seen = new LinkedHashSet<String>();
        for (String h : headers) {
            String key = h;
            int i = 1;
            while (!seen.add(key)) key = h + "_" + (i++);
            keys.add(key);
            table.addColumn(ReportTable.Column.of(key, h));
        }
        for (List<String> row : rows) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int c = 0; c < keys.size() && c < row.size(); c++) {
                rowMap.put(keys.get(c), row.get(c));
            }
            table.addRow(rowMap);
        }
        return table;
    }

    private static String cellText(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> cell.getStringCellValue();
        };
    }
}
