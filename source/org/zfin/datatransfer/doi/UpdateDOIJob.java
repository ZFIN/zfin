package org.zfin.datatransfer.doi;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.util.ReportGenerator;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 */
public class UpdateDOIJob extends AbstractValidateDataReportTask {

    private static Logger logger = Logger.getLogger(UpdateDOIJob.class) ;

    private boolean reportAll;
    private int maxToProcess;
    private int maxAttempts;

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        DOIProcessor driver = new DOIProcessor(reportAll, maxAttempts, maxToProcess);
        driver.findAndUpdateDOIs();

        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.includeTimestamp();
        for (String message : driver.getMessages()) {
            rg.addIntroParagraph(message);
        }
        List<List<String>> updated = driver.getUpdated();
        rg.addDataTable(updated.size() + " Updated Publications", Arrays.asList("Publication", "DOI"), updated);
        for (String error : driver.getErrors()) {
            rg.addErrorMessage(error);
        }
        rg.writeFiles(new File(dataDirectory, jobName), jobName);

        return driver.getErrors().size();
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        UpdateDOIJob job = new UpdateDOIJob();
        job.setPropertyFilePath(args[0]);
        job.init(args[1]);
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
        System.exit(job.execute());
    }
}
