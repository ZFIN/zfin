package org.zfin.wiki.jobs;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.WikiSynchronizationReport;
import org.zfin.wiki.service.AntibodyWikiWebService;
import org.zfin.wiki.service.WikiWebService;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 */
public class AntibodyWikiSynchronizationJob extends AbstractValidateDataReportTask {

    private static final Logger logger = Logger.getLogger(AntibodyWikiSynchronizationJob.class);

    public static void main(String[] args) throws WikiLoginException, FileNotFoundException, InterruptedException {
        initLog4J();
        setLoggerToInfoLevel(logger);
        setLoggerToInfoLevel(WikiSynchronizationReport.LOG);
        setLoggerToInfoLevel(AntibodyWikiWebService.logger);
        setLoggerToInfoLevel(WikiWebService.logger);
        logger.info("pushing antibodies to antibody wiki");
        String propertyFilePath = args[0];
        String jobDirectoryString = args[1];
        AntibodyWikiSynchronizationJob job = new AntibodyWikiSynchronizationJob();
        job.setPropertyFilePath(propertyFilePath);
        job.setJobName(args[2]);
        job.init(jobDirectoryString);
        job.execute();
    }


    @Override
    protected void addCustomVariables(Map<String, Object> map) {
        map.put("updatedAntibodies", report.getUpdatedPages());
        map.put("createdAntibodies", report.getCreatedPages());
        map.put("droppedAntibodies", report.getDroppedPages());
    }

    private void createReportFiles(WikiSynchronizationReport report) {
        if (report == null)
            return;
        String templateName = jobName + ".updated-antibodies";
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, templateName, true);
        createErrorReport(null, getStringifiedList(report.getUpdatedPages()), reportConfiguration);

        templateName = jobName + ".created-antibodies";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, templateName, true);
        createErrorReport(null, getStringifiedList(report.getCreatedPages()), reportConfiguration);

        templateName = jobName + ".dropped-antibodies";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, templateName, true);
        createErrorReport(null, getStringifiedList(report.getDroppedPages()), reportConfiguration);

        System.out.print(report);
    }

    private WikiSynchronizationReport report = null;

    @Override
    public void execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();
        try {
            report = AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
        } catch (FileNotFoundException | WikiLoginException e) {
            logger.error(e);
        }
        if (report != null && report.hasChanges())
            createReportFiles(report);

        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, jobName + ".statistics", false);
        createErrorReport(null, null, reportConfiguration);
    }
}
