package org.zfin.sequence.gff;

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
 * Fluent builder for the GFF3 / Priority-Pipeline / Ensembl-transcript load
 * reports. Produces a {@link ZfinReport} that flows through the unified report
 * viewer ({@code source/org/zfin/report/report-template.html}) via
 * {@link LegacyReportAdapter} and {@link ReportWriter} — the same path used by
 * the UniProt and NCBI gene loads.
 *
 * <p>The public API (setTitle/setReleaseID/addSummaryTable/SummaryTable methods)
 * is unchanged from the previous ObjectNode-producing version, so existing
 * callers only need to update the build/render step.
 */
public class ReportBuilder {

    private String title;
    private String releaseID;
    private String description;
    private final List<SummaryTable> summaryTables = new ArrayList<>();

    public ReportBuilder() {
        this.title = "Load Report";
        this.releaseID = "";
        this.description = "Summary of load report";
    }

    public ReportBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public ReportBuilder setReleaseID(String releaseID) {
        this.releaseID = releaseID;
        return this;
    }

    public ReportBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public SummaryTable addSummaryTable(String description) {
        SummaryTable table = new SummaryTable(description);
        summaryTables.add(table);
        return table;
    }

    public ZfinReport build() {
        LoadReportMeta meta = new LoadReportMeta(title, releaseID, System.currentTimeMillis());

        List<LoadReportSummaryTable> tables = new ArrayList<>();
        for (SummaryTable t : summaryTables) tables.add(t.build());

        ZfinReport report = new ZfinReport();
        report.setMeta(meta);
        report.setSummary(new LoadReportSummary(description, tables));
        report.setSupplementalData(Collections.emptyMap());
        report.setActions(Collections.emptyList());
        return report;
    }

    /** Convenience: build, adapt, and write the HTML report in one call. */
    public void writeHtmlReport(File output) throws IOException {
        ZfinReport zfinReport = build();
        new ReportWriter().write(new LegacyReportAdapter().adapt(zfinReport), output);
    }

    public class SummaryTable {
        private final String description;
        private final List<String> headerKeys = new ArrayList<>();
        private final List<String> headerTitles = new ArrayList<>();
        private final List<Map<String, Object>> rows = new ArrayList<>();

        public SummaryTable(String description) {
            this.description = description;
        }

        public SummaryTable setHeaders(String[] keys, String[] titles) {
            if (keys.length != titles.length) {
                throw new IllegalArgumentException("Keys and titles arrays must have the same length");
            }
            headerKeys.clear();
            headerTitles.clear();
            for (int i = 0; i < keys.length; i++) {
                headerKeys.add(keys[i]);
                headerTitles.add(titles[i]);
            }
            return this;
        }

        public SummaryTable setHeaders(List<String> keys) {
            headerKeys.clear();
            headerTitles.clear();
            for (String key : keys) {
                headerKeys.add(key);
                headerTitles.add(key);
            }
            return this;
        }

        public SummaryTable addBeforeAfterCountSummaryRow(String title, int beforeCount, int afterCount) {
            if (headerKeys.isEmpty()) {
                throw new IllegalStateException("Headers must be set before adding rows");
            }
            Map<String, String> row = new LinkedHashMap<>();
            row.put(headerKeys.get(0), title);
            row.put(headerKeys.get(1), String.valueOf(beforeCount));
            row.put(headerKeys.get(2), String.valueOf(afterCount));
            row.put(headerKeys.get(3), percentageDisplay(beforeCount, afterCount));
            return addSummaryRow(row);
        }

        public SummaryTable addSummaryRow(List<String> row) {
            if (row.size() != headerKeys.size()) {
                throw new IllegalArgumentException("Row size must match header size");
            }
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < row.size(); i++) {
                rowMap.put(headerKeys.get(i), row.get(i));
            }
            rows.add(rowMap);
            return this;
        }

        public SummaryTable addSummaryRow(Map<String, String> row) {
            Map<String, Object> objRow = new LinkedHashMap<>();
            objRow.putAll(row);
            rows.add(objRow);
            return this;
        }

        LoadReportSummaryTable build() {
            List<LoadReportTableHeader> headers = new ArrayList<>();
            for (int i = 0; i < headerKeys.size(); i++) {
                headers.add(new LoadReportTableHeader(headerKeys.get(i), headerTitles.get(i)));
            }
            return new LoadReportSummaryTable(description, headers, new ArrayList<>(rows));
        }
    }

    private static String percentageDisplay(int before, int after) {
        if (before == 0) {
            return after == 0 ? "0%" : "N/A";
        }
        double percentage = ((double) (after - before) / before) * 100;
        return String.format("%.2f%%", percentage);
    }
}
