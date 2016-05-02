package org.zfin.wiki.jobs;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.util.ReportGenerator;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.WikiSynchronizationReport;
import org.zfin.wiki.service.AntibodyWikiWebService;
import org.zfin.wiki.service.WikiWebService;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class AntibodyWikiSynchronizationJob extends AbstractValidateDataReportTask {

    private static final Logger logger = Logger.getLogger(AntibodyWikiSynchronizationJob.class);

    public AntibodyWikiSynchronizationJob(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] args) throws WikiLoginException, FileNotFoundException, InterruptedException {
        initLog4J();
        setLoggerToInfoLevel(logger);
        setLoggerToInfoLevel(WikiSynchronizationReport.LOG);
        setLoggerToInfoLevel(AntibodyWikiWebService.logger);
        setLoggerToInfoLevel(WikiWebService.logger);
        logger.info("pushing antibodies to antibody wiki");
        String propertyFilePath = args[0];
        String jobDirectoryString = args[1];
        AntibodyWikiSynchronizationJob job = new AntibodyWikiSynchronizationJob(args[2], propertyFilePath, jobDirectoryString);
        job.initDatabase(true);
        System.exit(job.execute());
    }

    private void createReportFiles(WikiSynchronizationReport report) {
        if (report == null) {
            return;
        }
        String reportName = jobName + ".updated-antibodies";
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, getStringifiedList(report.getUpdatedPages()), reportConfiguration);

        reportName = jobName + ".created-antibodies";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, getStringifiedList(report.getCreatedPages()), reportConfiguration);

        reportName = jobName + ".dropped-antibodies";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, getStringifiedList(report.getDroppedPages()), reportConfiguration);

        System.out.print(report);
    }

    private WikiSynchronizationReport report = null;

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();
        int exitCode = 0;
        try {
            report = AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
        } catch (FileNotFoundException | WikiLoginException e) {
            logger.error(e);
            exitCode = 1;
        }
        if (report != null && report.hasChanges()) {
            createReportFiles(report);
        }

        ReportGenerator statistics = new ReportGenerator();
        Map<String, Object> summary = new HashMap<>();
        summary.put("Created Wiki Pages", report.getCreatedPages().size());
        summary.put("Updated Wiki Pages", report.getUpdatedPages().size());
        summary.put("Dropped Wiki Pages", report.getDroppedPages().size());
        statistics.setReportTitle("Report for " + jobName);
        statistics.includeTimestamp();
        statistics.addSummaryTable(summary);
        statistics.writeFiles(dataDirectory, jobName + ".statistics");
        return exitCode;
    }
}
