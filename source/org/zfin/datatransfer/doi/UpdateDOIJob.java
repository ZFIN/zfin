package org.zfin.datatransfer.doi;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;

/**
 */
public class UpdateDOIJob extends AbstractValidateDataReportTask {

    private static Logger logger = Logger.getLogger(UpdateDOIJob.class) ;

    private boolean reportAll;
    private int maxToProcess;
    private int maxAttempts;

    @Override
    public void execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        DOIProcessor driver = new DOIProcessor(reportAll, maxAttempts, maxToProcess);
        driver.findAndUpdateDOIs();

        String infoReportName = jobName + ".info";
        ReportConfiguration infoReport = new ReportConfiguration(jobName, dataDirectory, infoReportName, true);
        createErrorReport(null, getStringifiedList(driver.getMessages()), infoReport);

        String errorReportName = jobName + ".errors";
        ReportConfiguration errorReport = new ReportConfiguration(jobName, dataDirectory, errorReportName, true);
        createErrorReport(null, getStringifiedList(driver.getErrors()), errorReport);
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        UpdateDOIJob job = new UpdateDOIJob();
        job.setPropertyFilePath(args[0]);
        job.setBaseDir(args[1]);
        String jobName = args[2];
        job.setJobName(jobName);
        if (jobName.endsWith("_d")) {
            job.reportAll = false;
            job.maxToProcess = 20;
            job.maxAttempts = DOIProcessor.ALL;
        } else if (jobName.endsWith("_m")) {
            job.reportAll = true;
            job.maxToProcess = DOIProcessor.ALL;
            job.maxToProcess = DOIProcessor.ALL;
        } else {
            throw new RuntimeException("Expecting job name to end in `_d` or `_m`, but was: " + jobName);
        }
        job.init();
        job.execute();
    }
}
