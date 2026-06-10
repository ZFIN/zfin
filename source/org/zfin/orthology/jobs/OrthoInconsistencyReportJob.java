package org.zfin.orthology.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.report.Report;
import org.zfin.report.ReportNode;
import org.zfin.report.ReportTable;
import org.zfin.report.ReportWriter;

import java.io.File;
import java.util.List;

/**
 * One-time scan of Human + Mouse orthologs whose zebrafish gene name and/or
 * symbol disagrees with the orthologue currently stored in {@code ortholog}.
 * Replaces the older Perl one-time TSV (ZFIN-10286).
 *
 * <p>Rule mirrors the existing weekly report (case-insensitive substring +
 * trailing-letter strip for paralog suffixes), applied independently to names
 * and symbols. Rows where both checks pass are dropped. The remaining rows
 * are rendered via the shared {@link Report} HTML viewer with character-level
 * diffs highlighted inside the offending cells.
 *
 * <p>Run via: {@code gradle :server_apps:DB_maintenance:oneTimeOrthoInconsistencyReport}.
 */
public class OrthoInconsistencyReportJob extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(OrthoInconsistencyReportJob.class);

    private static final String SCHEMA_INCONSISTENCY = "orthoInconsistency";

    public OrthoInconsistencyReportJob(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        OrthoInconsistencyReportJob job = new OrthoInconsistencyReportJob(args[2], args[0], args[1]);
        job.initDatabase(true);
        System.exit(job.execute());
    }

    @Override
    public int execute() {
        setLoggerFile();
        clearReportDirectory();

        try {
            List<Row> rows = fetch();
            logger.info("Scanned {} Human/Mouse orthologs", rows.size());

            List<Row> inconsistent = rows.stream().filter(Row::isInconsistent).toList();
            logger.info("{} inconsistencies (name and/or symbol)", inconsistent.size());

            File html = new File(new File(dataDirectory, jobName), jobName + ".html");
            new ReportWriter().write(buildReport(inconsistent, rows.size()), html);
            logger.info("HTML report written to: {}", html.getAbsolutePath());
            System.out.println("HTML report written to: " + html.getAbsolutePath());
            return 0;
        } catch (Exception e) {
            logger.error("ortho inconsistency report failed", e);
            return 1;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // -------- data --------

    private List<Row> fetch() {
        String hql = """
                select o.zebrafishGene.zdbID,
                       o.zebrafishGene.abbreviation,
                       o.zebrafishGene.name,
                       o.organism.commonName,
                       o.symbol,
                       o.name
                  from Ortholog o
                 where o.organism.commonName in ('Human', 'Mouse')
                   and o.zebrafishGene.markerType.name = 'GENE'
                 order by o.zebrafishGene.abbreviation
                """;
        List<Object[]> raw = HibernateUtil.currentSession()
            .createQuery(hql, Object[].class)
            .list();
        return raw.stream()
            .map(r -> new Row(
                (String) r[0],
                (String) r[1],
                (String) r[2],
                (String) r[3],
                (String) r[4],
                (String) r[5]))
            .toList();
    }

    /**
     * Same comparison rule the weekly report uses: lowercased, commas removed,
     * substring check in both directions; the chopped ZFIN side relaxes the
     * trailing-letter (paralog suffix) for "mybphb" ↔ "MYBPH" cases.
     */
    private static boolean matches(String zfin, String other) {
        if (zfin == null || other == null) return false;
        String zfNorm = norm(zfin);
        String otNorm = norm(other);
        if (zfNorm.contains(otNorm)) return true;
        String chopped = zfNorm.isEmpty() ? zfNorm : zfNorm.substring(0, zfNorm.length() - 1);
        return otNorm.contains(chopped) && !chopped.isEmpty();
    }

    private static String norm(String s) {
        return s.toLowerCase().replace(",", "");
    }

    // -------- report --------

    private Report buildReport(List<Row> rows, int totalScanned) {
        Report report = new Report()
            .meta(new Report.Meta()
                .title(jobName + " — ortholog name/symbol inconsistencies")
                .createdAt(System.currentTimeMillis())
                .schemaVersion("1"))
            .definitions(defs());

        ReportNode root = new ReportNode()
            .id("_root")
            .title(jobName)
            .body(Report.Body.text(
                "One-time scan of Human + Mouse orthologs whose stored name or symbol " +
                "disagrees with the ZFIN gene's. " + totalScanned + " orthologs scanned; " +
                rows.size() + " inconsistencies found.\n\n" +
                "Match rule: case-insensitive substring with a one-character trailing strip " +
                "for zebrafish paralog suffixes (so `mybphb` matches `MYBPH`). Diff highlights " +
                "show character-level differences inside each cell."));

        ReportTable table = new ReportTable()
            .schemaRef(SCHEMA_INCONSISTENCY)
            .title("Inconsistencies (" + rows.size() + ")");
        for (Row r : rows) {
            String zfinName  = orEmpty(r.zfinName);
            String orthoName = orEmpty(r.orthoName);
            String zfinSym   = orEmpty(r.zfinSymbol);
            String orthoSym  = orEmpty(r.orthoSymbol);

            boolean nameOk   = matches(zfinName, orthoName);
            boolean symbolOk = matches(zfinSym,  orthoSym);
            String diffType  = (!nameOk && !symbolOk) ? "Both"
                             : (!nameOk)              ? "Name"
                             :                          "Symbol";

            table.addRow(
                "zdbID",     r.zdbId,
                "organism",  r.organism,
                "diffType",  diffType,
                "zfinSym",   symbolOk ? zfinSym  : OrthoNameDiff.highlightOld(zfinSym,  orthoSym),
                "orthoSym",  symbolOk ? orthoSym : OrthoNameDiff.highlightNew(zfinSym,  orthoSym),
                "zfinName",  nameOk   ? zfinName : OrthoNameDiff.highlightOld(zfinName, orthoName),
                "orthoName", nameOk   ? orthoName: OrthoNameDiff.highlightNew(zfinName, orthoName));
        }
        root.addTable(table);
        report.root(root);
        return report;
    }

    private Report.Definitions defs() {
        return new Report.Definitions()
            .field("zdbID",     Report.FieldDef.link("ZDB ID", "https://zfin.org/{value}"))
            .field("htmlCell",  Report.FieldDef.html("Value"))
            .tableSchema(SCHEMA_INCONSISTENCY, new Report.TableSchema()
                .description("One row per inconsistent ortholog; diff-highlighted cells use <mark>.")
                .addColumn(ReportTable.Column.of("zdbID",     "ZDB ID",      "zdbID"))
                .addColumn(ReportTable.Column.of("organism",  "Organism"))
                .addColumn(ReportTable.Column.of("diffType",  "Diff"))
                .addColumn(ReportTable.Column.of("zfinSym",   "ZFIN symbol",   "htmlCell"))
                .addColumn(ReportTable.Column.of("orthoSym",  "Ortho symbol",  "htmlCell"))
                .addColumn(ReportTable.Column.of("zfinName",  "ZFIN name",     "htmlCell"))
                .addColumn(ReportTable.Column.of("orthoName", "Ortho name",    "htmlCell")));
    }

    private static String orEmpty(String s) { return s == null ? "" : s; }

    private record Row(String zdbId, String zfinSymbol, String zfinName,
                       String organism, String orthoSymbol, String orthoName) {
        boolean isInconsistent() {
            return !matches(orEmpty(zfinName), orEmpty(orthoName))
                || !matches(orEmpty(zfinSymbol), orEmpty(orthoSymbol));
        }
    }
}
