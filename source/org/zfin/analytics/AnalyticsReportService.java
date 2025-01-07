package org.zfin.analytics;

import java.nio.file.Path;
import java.util.List;

public class AnalyticsReportService {
    public static List<Path> runReportWithDates(AnalyticsReportDefinition reportDefinition, String credentials, Path outputDirectory, String start, String end) {
        assertDateFormat(start);
        assertDateFormat(end);
        reportDefinition.setStartDate(start);
        reportDefinition.setEndDate(end);

        return runReport(reportDefinition, credentials, outputDirectory);
    }

    private static List<Path> runReport(AnalyticsReportDefinition reportDefinition, String credentials, Path outputDirectory) {
        return runReportAndWriteCsvFiles(reportDefinition, credentials, outputDirectory);
    }

    private static void assertDateFormat(String start) {
        if (start.length() != 10) {
            throw new IllegalArgumentException("Date format should be YYYY-MM-DD");
        }
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        if (!start.matches(regex)) {
            throw new IllegalArgumentException("Date format should be YYYY-MM-DD");
        }
    }

    public static List<Path> runReportAndWriteCsvFiles(AnalyticsReportDefinition reportDefinition, String credentials, Path outputDirectory) {
        AnalyticsReportRunner analyticsReportRunner = new AnalyticsReportRunner(reportDefinition.convertToAnalyticsReportRunnerConfig(), credentials);
        List<Path> outputFiles = analyticsReportRunner.runReportAndWriteCsvFiles(outputDirectory);
        return outputFiles;
    }


}
