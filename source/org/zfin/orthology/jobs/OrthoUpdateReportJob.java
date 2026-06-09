package org.zfin.orthology.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportTable;
import org.zfin.report.ReportWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around the {@code Update-Orthology_w} Perl pipeline
 * ({@code server_apps/data_transfer/ORTHO/runOrthology.pl}).
 *
 * <p>The Perl owns all the work — downloading NCBI data, updating orthology, and
 * writing seven flat text files. This job execs it, then parses those files into
 * a single {@link Report} HTML page with a left-nav section per output group.
 * The flat {@code .txt} files are left untouched so Jenkins keeps emailing them
 * as before; the HTML is purely additive.
 *
 * <p>No database access happens here — the Perl does it all. We just run a
 * process and read its output files.
 *
 * <p>Run via: {@code gradle :server_apps:DB_maintenance:updateOrthologyReport}.
 */
public class OrthoUpdateReportJob extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(OrthoUpdateReportJob.class);

    private static final String PERL_SCRIPT = "./runOrthology.pl";

    // Output file names produced by the Perl pipeline.
    private static final String FILE_STATISTICS        = "ortho_statistics.txt";
    private static final String FILE_INCONSISTENT_NEW  = "orthoInconsistentZebrafishGeneNamesReport.txt";
    private static final String FILE_INCONSISTENT_FULL = "orthoInconsistentZebrafishGeneNamesReport_persistent.txt";
    private static final String FILE_NAMES_UPDATED     = "orthoNamesUpdateList.txt";
    private static final String FILE_OBSOLETE          = "ortho_obsolete.txt";
    private static final String FILE_NOT_FOUND         = "orthoNcbiIdsNotFoundReport.txt";

    // Table schema keys.
    private static final String SCHEMA_STATISTICS  = "orthoStatistics";
    private static final String SCHEMA_INCONSISTENT = "orthoInconsistent";
    private static final String SCHEMA_INCONSISTENT_DATED = "orthoInconsistentDated";
    private static final String SCHEMA_NAMES_UPDATED = "orthoNamesUpdated";
    private static final String SCHEMA_OBSOLETE    = "orthoObsolete";
    private static final String SCHEMA_NOT_FOUND   = "orthoNotFound";

    /** Working directory for the Perl script; also where its output files land. */
    private final File orthoDir;

    public OrthoUpdateReportJob(String jobName, String propertyFilePath, String dataDirectoryString, String orthoDirString) {
        super(jobName, propertyFilePath, dataDirectoryString);
        this.orthoDir = new File(orthoDirString);
    }

    /** Test-only: skips property/report-config init; just enough state to build a report. */
    OrthoUpdateReportJob(String jobName, File orthoDir) {
        super();
        this.jobName = jobName;
        this.orthoDir = orthoDir;
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        // args: propertiesPath, report_data dir, jobName, orthoDir
        OrthoUpdateReportJob job = new OrthoUpdateReportJob(args[2], args[0], args[1], args[3]);
        System.exit(job.execute());
    }

    @Override
    public int execute() {
        setLoggerFile();
        clearReportDirectory();

        int perlExit = runPerl();

        try {
            File html = new File(new File(dataDirectory, jobName), jobName + ".html");
            new ReportWriter().write(buildReport(perlExit), html);
            logger.info("HTML report written to: {}", html.getAbsolutePath());
            System.out.println("HTML report written to: " + html.getAbsolutePath());
        } catch (Exception e) {
            logger.error("failed to build ortho update report", e);
            // Don't mask a Perl failure with a report-build failure, but if the
            // Perl succeeded and only the report failed, surface that.
            if (perlExit == 0) return 1;
        }
        return perlExit;
    }

    // -------- perl --------

    private int runPerl() {
        if (!orthoDir.isDirectory()) {
            logger.error("ORTHO directory not found: {}", orthoDir.getAbsolutePath());
            return 1;
        }
        logger.info("Running {} in {}", PERL_SCRIPT, orthoDir.getAbsolutePath());
        try {
            // inheritIO streams the Perl's stdout/stderr straight to the Jenkins
            // console; the child inherits our environment (ROOT_PATH, DB_NAME, …).
            Process process = new ProcessBuilder(PERL_SCRIPT)
                .directory(orthoDir)
                .inheritIO()
                .start();
            int exit = process.waitFor();
            logger.info("{} exited with code {}", PERL_SCRIPT, exit);
            if (exit != 0) {
                logger.error("Perl orthology pipeline failed (exit {}); building report from whatever output exists", exit);
            }
            return exit;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            logger.error("Failed to run " + PERL_SCRIPT, e);
            return 1;
        }
    }

    // -------- report --------

    Report buildReport(int perlExit) {
        Report report = new Report()
            .meta(new Report.Meta()
                .title(jobName + " — orthology update report")
                .createdAt(System.currentTimeMillis())
                .schemaVersion("1"))
            .definitions(defs());

        StringBuilder summary = new StringBuilder()
            .append("Report for the weekly Update-Orthology Perl pipeline. ")
            .append("Sections below mirror the text files emailed by Jenkins.\n\n");
        if (perlExit != 0) {
            summary.append("⚠ The Perl pipeline exited with code ").append(perlExit)
                   .append("; sections may be incomplete.\n\n");
        }
        summary.append("Match/diff highlighting (<mark>) shows character-level differences inside cells.");

        ReportNode root = new ReportNode()
            .id("_root")
            .title(jobName)
            .body(Report.Body.text(summary.toString()));

        root.addChild(buildStatisticsNode());
        root.addChild(buildInconsistentNode());
        root.addChild(buildNamesUpdatedNode());
        root.addChild(buildObsoleteAndNotFoundNode());

        report.root(root);
        return report;
    }

    private ReportNode buildStatisticsNode() {
        List<String[]> rows = readDelimited(orthoDir, FILE_STATISTICS, "\t");
        ReportNode node = new ReportNode().id("statistics").title("Load statistics");
        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_STATISTICS)
            .title("Orthology load tracking (last 30 days)");

        // The first row is a literal header row emitted by the SQL view
        // (col 0 == "load name"); use it for column titles when present.
        int dataStart = 0;
        if (!rows.isEmpty() && rows.get(0).length > 0 && "load name".equalsIgnoreCase(rows.get(0)[0].trim())) {
            dataStart = 1;
        }
        long count = 0;
        for (int i = dataStart; i < rows.size(); i++) {
            String[] r = rows.get(i);
            table.addRow(
                "loadName", col(r, 0),
                "lastRun",  col(r, 1),
                "mgi",      col(r, 2),
                "hgnc",     col(r, 3),
                "omim",     col(r, 4),
                "gene",     col(r, 5),
                "flybase",  col(r, 6));
            count++;
        }
        node.count(count);
        node.addTable(table);
        return node;
    }

    private ReportNode buildInconsistentNode() {
        List<IncRow> fresh = parseInconsistencyBlocks(orthoDir, FILE_INCONSISTENT_NEW);
        List<IncRow> full  = parseInconsistencyBlocks(orthoDir, FILE_INCONSISTENT_FULL);

        ReportNode node = new ReportNode().id("inconsistent").title("Inconsistent ZF gene names");
        node.count((long) (fresh.size() + full.size()));

        node.addTable(inconsistencyTable(fresh, SCHEMA_INCONSISTENT,
            "New this run (" + fresh.size() + ")", false));
        node.addTable(inconsistencyTable(full, SCHEMA_INCONSISTENT_DATED,
            "Full current surface (" + full.size() + ")", true));
        return node;
    }

    private ReportTable inconsistencyTable(List<IncRow> rows, String schema, String title, boolean dated) {
        ReportTable table = new ReportTable().schemaRef(schema).title(title);
        for (IncRow r : rows) {
            // Diff the ZFIN gene name against whichever ortholog name is present.
            String hName = orEmpty(r.hName);
            String mName = orEmpty(r.mName);
            String zName = orEmpty(r.zName);
            if (dated) {
                table.addRow(
                    "firstSeen", orEmpty(r.firstSeen),
                    "zdbID",     r.zdbId,
                    "symbol",    orEmpty(r.symbol),
                    "zfinName",  OrthoNameDiff.highlightOld(zName, !hName.isEmpty() ? hName : mName),
                    "humanName", hName.isEmpty() ? "" : OrthoNameDiff.highlightNew(zName, hName),
                    "mouseName", mName.isEmpty() ? "" : OrthoNameDiff.highlightNew(zName, mName));
            } else {
                table.addRow(
                    "zdbID",     r.zdbId,
                    "symbol",    orEmpty(r.symbol),
                    "zfinName",  OrthoNameDiff.highlightOld(zName, !hName.isEmpty() ? hName : mName),
                    "humanName", hName.isEmpty() ? "" : OrthoNameDiff.highlightNew(zName, hName),
                    "mouseName", mName.isEmpty() ? "" : OrthoNameDiff.highlightNew(zName, mName));
            }
        }
        return table;
    }

    private ReportNode buildNamesUpdatedNode() {
        // orthoNamesUpdateList.txt: zdbId|species|oldName|newName|
        List<String[]> rows = readDelimited(orthoDir, FILE_NAMES_UPDATED, "\\|");
        ReportNode node = new ReportNode().id("namesUpdated").title("Ortholog names updated");
        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_NAMES_UPDATED)
            .title("Ortholog names changed to match NCBI (" + rows.size() + ")");
        for (String[] r : rows) {
            String oldName = col(r, 2);
            String newName = col(r, 3);
            table.addRow(
                "zdbID",   col(r, 0),
                "species", col(r, 1),
                "oldName", OrthoNameDiff.highlightOld(oldName, newName),
                "newName", OrthoNameDiff.highlightNew(oldName, newName));
        }
        node.count((long) rows.size());
        node.addTable(table);
        return node;
    }

    private ReportNode buildObsoleteAndNotFoundNode() {
        // ortho_obsolete.txt: mrkr_zdb_id|mrkr_abbrev|mrkr_name|ortho_symbol|ortho_ncbi_id|ortho_name|evidence|pub
        List<String[]> obsolete = readDelimited(orthoDir, FILE_OBSOLETE, "\\|");
        // orthoNcbiIdsNotFoundReport.txt: zdbGeneId\tzdbGeneAbbrev\tncbiID\torganism
        List<String[]> notFound = readDelimited(orthoDir, FILE_NOT_FOUND, "\t");

        ReportNode node = new ReportNode().id("obsoleteNotFound").title("Obsolete & NCBI ids not found");
        node.count((long) (obsolete.size() + notFound.size()));

        ReportTable obsoleteTable = new ReportTable()
            .schemaRef(SCHEMA_OBSOLETE)
            .title("Obsolete NCBI gene ids (" + obsolete.size() + ")");
        for (String[] r : obsolete) {
            obsoleteTable.addRow(
                "zdbID",       col(r, 0),
                "abbrev",      col(r, 1),
                "name",        col(r, 2),
                "orthoSymbol", col(r, 3),
                "ncbiId",      col(r, 4),
                "orthoName",   col(r, 5),
                "evidence",    col(r, 6),
                "pub",         col(r, 7));
        }
        node.addTable(obsoleteTable);

        ReportTable notFoundTable = new ReportTable()
            .schemaRef(SCHEMA_NOT_FOUND)
            .title("NCBI ids not found (" + notFound.size() + ")");
        for (String[] r : notFound) {
            notFoundTable.addRow(
                "zdbID",    col(r, 0),
                "abbrev",   col(r, 1),
                "ncbiId",   col(r, 2),
                "organism", col(r, 3));
        }
        node.addTable(notFoundTable);
        return node;
    }

    private Report.Definitions defs() {
        return new Report.Definitions()
            .field("zdbID",    Report.FieldDef.link("ZDB ID", "https://zfin.org/{value}"))
            .field("htmlCell", Report.FieldDef.html("Value"))
            .tableSchema(SCHEMA_STATISTICS, new Report.TableSchema()
                .description("Orthology load-tracking link counts over the last 30 days.")
                .addColumn(ReportTable.Column.of("loadName", "Load name"))
                .addColumn(ReportTable.Column.of("lastRun",  "Last run"))
                .addColumn(ReportTable.Column.of("mgi",      "MGI links"))
                .addColumn(ReportTable.Column.of("hgnc",     "HGNC links"))
                .addColumn(ReportTable.Column.of("omim",     "OMIM links"))
                .addColumn(ReportTable.Column.of("gene",     "GENE links"))
                .addColumn(ReportTable.Column.of("flybase",  "FLYBASE links")))
            .tableSchema(SCHEMA_INCONSISTENT, new Report.TableSchema()
                .description("ZFIN gene names inconsistent with the NCBI ortholog name — new since the last run.")
                .addColumn(ReportTable.Column.of("zdbID",     "ZDB ID", "zdbID"))
                .addColumn(ReportTable.Column.of("symbol",    "Symbol"))
                .addColumn(ReportTable.Column.of("zfinName",  "ZFIN gene name", "htmlCell"))
                .addColumn(ReportTable.Column.of("humanName", "Human ortholog name", "htmlCell"))
                .addColumn(ReportTable.Column.of("mouseName", "Mouse ortholog name", "htmlCell")))
            .tableSchema(SCHEMA_INCONSISTENT_DATED, new Report.TableSchema()
                .description("Full current set of ZFIN/ortholog name inconsistencies, newest first.")
                .addColumn(ReportTable.Column.of("firstSeen", "First seen"))
                .addColumn(ReportTable.Column.of("zdbID",     "ZDB ID", "zdbID"))
                .addColumn(ReportTable.Column.of("symbol",    "Symbol"))
                .addColumn(ReportTable.Column.of("zfinName",  "ZFIN gene name", "htmlCell"))
                .addColumn(ReportTable.Column.of("humanName", "Human ortholog name", "htmlCell"))
                .addColumn(ReportTable.Column.of("mouseName", "Mouse ortholog name", "htmlCell")))
            .tableSchema(SCHEMA_NAMES_UPDATED, new Report.TableSchema()
                .description("Ortholog names that were changed to match NCBI.")
                .addColumn(ReportTable.Column.of("zdbID",   "ZDB ID", "zdbID"))
                .addColumn(ReportTable.Column.of("species", "Species"))
                .addColumn(ReportTable.Column.of("oldName", "Old name (ZFIN)", "htmlCell"))
                .addColumn(ReportTable.Column.of("newName", "New name (NCBI)", "htmlCell")))
            .tableSchema(SCHEMA_OBSOLETE, new Report.TableSchema()
                .description("Orthologs whose NCBI gene id is flagged obsolete.")
                .addColumn(ReportTable.Column.of("zdbID",       "ZDB ID", "zdbID"))
                .addColumn(ReportTable.Column.of("abbrev",      "Gene symbol"))
                .addColumn(ReportTable.Column.of("name",        "Gene name"))
                .addColumn(ReportTable.Column.of("orthoSymbol", "Ortholog symbol"))
                .addColumn(ReportTable.Column.of("ncbiId",      "NCBI gene id"))
                .addColumn(ReportTable.Column.of("orthoName",   "Ortholog name"))
                .addColumn(ReportTable.Column.of("evidence",    "Evidence"))
                .addColumn(ReportTable.Column.of("pub",         "Pub")))
            .tableSchema(SCHEMA_NOT_FOUND, new Report.TableSchema()
                .description("NCBI gene ids referenced by an ortholog but not found in the NCBI data.")
                .addColumn(ReportTable.Column.of("zdbID",    "ZDB ID", "zdbID"))
                .addColumn(ReportTable.Column.of("abbrev",   "Gene symbol"))
                .addColumn(ReportTable.Column.of("ncbiId",   "NCBI gene id"))
                .addColumn(ReportTable.Column.of("organism", "Organism")));
    }

    // -------- parsing --------

    /**
     * Reads a delimited file from the ORTHO directory into a list of column
     * arrays. Blank lines are skipped. Missing file yields an empty list (the
     * report just shows zero rows for that section).
     */
    static List<String[]> readDelimited(File orthoDir, String fileName, String regex) {
        List<String[]> rows = new ArrayList<>();
        File file = new File(orthoDir, fileName);
        if (!file.isFile()) {
            logger.warn("Expected output file not found: {}", file.getAbsolutePath());
            return rows;
        }
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                // -1 limit keeps trailing empty fields (e.g. the trailing pipe).
                rows.add(line.split(regex, -1));
            }
        } catch (IOException e) {
            logger.error("Failed to read " + file.getAbsolutePath(), e);
        }
        return rows;
    }

    /**
     * Parses the block-format inconsistency reports. Each block:
     * <pre>
     * [first seen: YYYY-MM-DD]   (persistent file only)
     * ZDB-GENE-xxx     symbol
     * gene name (z): ...
     * gene name (h): ...
     * gene symbol (h): ...
     * gene name (m): ...
     * gene symbol (m): ...
     * </pre>
     * Blocks are separated by blank lines; {@code #} comment lines are ignored.
     */
    static List<IncRow> parseInconsistencyBlocks(File orthoDir, String fileName) {
        List<IncRow> result = new ArrayList<>();
        File file = new File(orthoDir, fileName);
        if (!file.isFile()) {
            logger.warn("Expected output file not found: {}", file.getAbsolutePath());
            return result;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to read " + file.getAbsolutePath(), e);
            return result;
        }

        Block block = new Block();
        for (String line : lines) {
            if (line.startsWith("#")) continue;
            if (line.isBlank()) {
                if (block.hasData()) result.add(block.toRow());
                block = new Block();
                continue;
            }
            block.accept(line);
        }
        if (block.hasData()) result.add(block.toRow());
        return result;
    }

    /** Mutable accumulator for one inconsistency block. */
    private static final class Block {
        String firstSeen, zdbId, symbol, zName, hName, hSym, mName, mSym;

        void accept(String line) {
            if (line.startsWith("first seen:")) {
                firstSeen = after(line, "first seen:");
            } else if (line.startsWith("gene name (z):")) {
                zName = after(line, "gene name (z):");
            } else if (line.startsWith("gene name (h):")) {
                hName = after(line, "gene name (h):");
            } else if (line.startsWith("gene symbol (h):")) {
                hSym = after(line, "gene symbol (h):");
            } else if (line.startsWith("gene name (m):")) {
                mName = after(line, "gene name (m):");
            } else if (line.startsWith("gene symbol (m):")) {
                mSym = after(line, "gene symbol (m):");
            } else if (zdbId == null && line.startsWith("ZDB")) {
                String[] parts = line.trim().split("\\s+", 2);
                zdbId = parts[0];
                symbol = parts.length > 1 ? parts[1] : "";
            }
        }

        boolean hasData() { return zdbId != null; }

        IncRow toRow() {
            return new IncRow(firstSeen, zdbId, symbol, zName, hName, hSym, mName, mSym);
        }

        private static String after(String line, String prefix) {
            return line.substring(prefix.length()).trim();
        }
    }

    record IncRow(String firstSeen, String zdbId, String symbol,
                  String zName, String hName, String hSym,
                  String mName, String mSym) {}

    // -------- small helpers --------

    private static String col(String[] row, int i) {
        return i < row.length ? row[i].trim() : "";
    }

    private static String orEmpty(String s) { return s == null ? "" : s; }
}
