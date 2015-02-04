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

    private static Logger logger = Logger.getLogger(UpdateDOIJob.class);

    private int maxToProcess;
    private int maxAttempts;

    public UpdateDOIJob(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        DOIProcessor driver = new DOIProcessor(maxAttempts, maxToProcess);
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
        String jobName = args[2];
        UpdateDOIJob job = new UpdateDOIJob(jobName, args[0], args[1]);
        job.maxToProcess = DOIProcessor.ALL;
        job.initDatabase();
        System.exit(job.execute());
    }
}
