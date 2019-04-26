package org.zfin.wiki.jobs;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.service.AntibodyWikiWebService;
import org.zfin.wiki.service.WikiWebService;

import java.io.FileNotFoundException;

/**
 * Cleans sandbox.
 */
public class SandboxCleanerJob extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(SandboxCleanerJob.class);

    public SandboxCleanerJob(String jobName, String propertyFilePath, String jobDirectoryString) {
        super(jobName, propertyFilePath, jobDirectoryString);
    }

    public int execute() {
        try {
            WikiWebService.getInstance(ZfinPropertiesEnum.WIKI_HOST.value()).cleanSandbox();
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public static void main(String[] args) throws WikiLoginException, FileNotFoundException, InterruptedException {
        initLog4J();
        setLoggerToInfoLevel(logger);
        setLoggerToInfoLevel(AbstractValidateDataReportTask.LOG);
        setLoggerToInfoLevel(AntibodyWikiWebService.logger);
        setLoggerToInfoLevel(WikiWebService.logger);
        logger.info("Cleaning sandbox wiki...");
        String propertyFilePath = args[0];
        String jobDirectoryString = args[1];
        SandboxCleanerJob job = new SandboxCleanerJob(args[2], propertyFilePath, jobDirectoryString);
        System.exit(job.execute());
    }
}
