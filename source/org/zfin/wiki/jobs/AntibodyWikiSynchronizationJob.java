package org.zfin.wiki.jobs;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.WikiSynchronizationReport;
import org.zfin.wiki.service.AntibodyWikiWebService;
import org.zfin.wiki.service.WikiWebService;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
        job.setBaseDir(jobDirectoryString);
        job.setJobName(args[2]);
        job.init();
        WikiSynchronizationReport report = AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
        job.createReportFiles(report);
    }

    private void createReportFiles(WikiSynchronizationReport wikiSynchronizationReport) {
        if (wikiSynchronizationReport == null)
            return;
        createErrorReport(null, getStringifiedList(wikiSynchronizationReport.getUpdatedPages()), "updated-antibodies");
        createErrorReport(null, getStringifiedList(wikiSynchronizationReport.getCreatedPages()), "created-antibodies");
        createErrorReport(null, getStringifiedList(wikiSynchronizationReport.getDroppedPages()), "dropped-antibodies");

        System.out.print(wikiSynchronizationReport);
    }

    @Override
    public void execute() {

    }
}
