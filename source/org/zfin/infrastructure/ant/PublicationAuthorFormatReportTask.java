package org.zfin.infrastructure.ant;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.hibernate.query.NativeQuery;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.agr.AuthorFormatChecker;
import org.zfin.marker.agr.AuthorFormatChecker.Category;
import org.zfin.marker.agr.AuthorFormatChecker.Finding;
import org.zfin.util.ReportGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ZFIN-8072: lists publications whose free-text authors field still needs a curator's hand after
 * {@link org.zfin.marker.agr.AuthorStringParser} has split it for the Alliance reference export.
 * Only publications without pubmed_publication_author rows are examined (the ones the export parses).
 * Publications with a blank or placeholder authors field go to a separate, labelled sheet.
 * Read-only: no DB updates. Run weekly (and on demand) from Jenkins (Check-Publication-Author-Formats_w).
 */
public class PublicationAuthorFormatReportTask extends AbstractValidateDataReportTask {

    private static final Logger LOG = LogManager.getLogger(PublicationAuthorFormatReportTask.class);

    static final String CLEANUP_CAPTION = "Needs curator cleanup";
    // Also the XLSX sheet name (Excel truncates at 31 characters), so keep the caption short.
    static final String MISSING_CAPTION = "Missing or placeholder author";
    static final String MISSING_LABEL = MISSING_CAPTION + " (policy pending, no action needed yet)";

    static final List<String> CLEANUP_HEADER = List.of("Category", "ZDB ID", "Pub date", "Type", "Title",
        "Current authors field", "Offending author(s)", "Suggested form", "What to fix");
    static final List<String> MISSING_HEADER = List.of("Kind", "ZDB ID", "Pub date", "Type", "Title",
        "Current authors field");

    public PublicationAuthorFormatReportTask(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    @Override
    public int execute() {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
        LOG.info("Job Name: " + jobName);
        clearReportDirectory();
        setLoggerFile();

        List<String> errorMessages = new ArrayList<>();
        Map<Category, List<List<String>>> cleanupByCategory = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            cleanupByCategory.put(category, new ArrayList<>());
        }
        List<List<String>> missing = new ArrayList<>();
        int examined = 0;

        try {
            // Native projection: no entity loading (see CheckSequenceOfReferenceDriftTask).
            // "not in" (not a correlated "not exists") so the planner hashes the subquery.
            String sql = "select p.zdb_id, to_char(p.pub_date, 'YYYY-MM-DD'), p.jtype, p.title, p.authors " +
                "  from publication p " +
                " where p.zdb_id not in (select a.ppa_publication_zdb_id from pubmed_publication_author a " +
                "                         where a.ppa_publication_zdb_id is not null) " +
                " order by p.zdb_id";
            NativeQuery<Object[]> query = HibernateUtil.currentSession().createNativeQuery(sql, Object[].class);
            for (Object[] row : query.list()) {
                examined++;
                List<String> pub = new ArrayList<>(List.of(
                    (String) row[0], nullToEmpty(row[1]), nullToEmpty(row[2]), nullToEmpty(row[3])));
                String authors = nullToEmpty(row[4]);
                classify(pub, authors, cleanupByCategory, missing);
            }
        } catch (Exception e) {
            LOG.error("Author format report failed", e);
            errorMessages.add(e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            HibernateUtil.closeSession();
        }

        List<List<String>> cleanup = new ArrayList<>();
        List<List<String>> summary = new ArrayList<>();
        for (Category category : Category.values()) {
            List<List<String>> rows = cleanupByCategory.get(category);
            summary.add(Arrays.asList(category.getLabel(), String.valueOf(rows.size())));
            cleanup.addAll(rows);
        }
        long blank = missing.stream().filter(r -> "Blank".equals(r.get(0))).count();
        LOG.info("Examined " + examined + " publications without pubmed authors; " + cleanup.size()
            + " cleanup rows; " + missing.size() + " missing/placeholder (" + blank + " blank)");

        ReportGenerator report = new ReportGenerator();
        report.setReportTitle("Report for " + jobName);
        report.includeTimestamp();
        report.addIntroParagraph(examined + " publications have no pubmed_publication_author rows and are exported to the "
            + "Alliance from the free-text authors field. " + cleanup.size() + " (publication, problem) rows below need a "
            + "curator to edit the authors field; \"and\" and \", and\" between authors are fine and are never reported.");
        report.addSummaryTable("Needs curator cleanup, rows per category", summary);
        report.addDataTable(CLEANUP_CAPTION, CLEANUP_HEADER, cleanup);
        report.addIntroParagraph(missing.size() + " publications have a blank or placeholder authors field (" + blank
            + " blank). " + MISSING_LABEL + ": this waits on a placeholder-author policy and is not part of the "
            + "count above.");
        report.addDataTable(MISSING_CAPTION, MISSING_HEADER, missing);
        for (String error : errorMessages) {
            report.addErrorMessage(error);
        }
        report.writeFiles(new java.io.File(dataDirectory, jobName), jobName);

        if (!errorMessages.isEmpty()) {
            throw new RuntimeException("Errors in " + jobName + ": " + errorMessages.size());
        }
        return 0;
    }

    /** Sorts one publication into the cleanup rows (one per category) or the missing/placeholder rows. */
    static void classify(List<String> pub, String authors,
                         Map<Category, List<List<String>>> cleanupByCategory, List<List<String>> missing) {
        if (AuthorFormatChecker.isBlank(authors) || AuthorFormatChecker.isPlaceholder(authors)) {
            List<String> row = new ArrayList<>(pub);
            row.add(0, AuthorFormatChecker.isBlank(authors) ? "Blank" : "Placeholder");
            row.add(authors);
            missing.add(row);
            return;
        }
        Map<Category, List<Finding>> byCategory = new EnumMap<>(Category.class);
        for (Finding finding : AuthorFormatChecker.check(authors)) {
            byCategory.computeIfAbsent(finding.category(), c -> new ArrayList<>()).add(finding);
        }
        for (Map.Entry<Category, List<Finding>> entry : byCategory.entrySet()) {
            List<Finding> findings = entry.getValue();
            List<String> row = new ArrayList<>();
            row.add(entry.getKey().getLabel());
            row.addAll(pub);
            row.add(authors);
            row.add(join(findings, Finding::author, "; "));
            row.add(join(findings, Finding::suggestion, "; "));
            row.add(join(findings, Finding::message, " "));
            cleanupByCategory.get(entry.getKey()).add(row);
        }
    }

    private static String join(List<Finding> findings, java.util.function.Function<Finding, String> f, String sep) {
        return findings.stream().map(f).filter(s -> !s.isEmpty()).reduce((a, b) -> a + sep + b).orElse("");
    }

    private static String nullToEmpty(Object o) {
        return o == null ? "" : o.toString();
    }

    public static void main(String[] args) {
        String instance = args[0];
        String jobName = args[1];
        String directory = args[2];
        String propertyFilePath = args[3];
        PublicationAuthorFormatReportTask task = new PublicationAuthorFormatReportTask(jobName, propertyFilePath, directory);
        task.setInstance(instance);
        task.initDatabase();
        System.exit(task.execute());
    }
}
