package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.util.ReportGenerator;

import java.io.File;

/**
 */
public class RegenerateWebHostCuratedDatabasesJob extends AbstractValidateDataReportTask {

    private static Logger logger = Logger.getLogger(RegenerateWebHostCuratedDatabasesJob.class);

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        logger.info("validating curated webhost database");
        int exitCode = 0;
        try {
            if (MountedWublastBlastService.getInstance().validateCuratedDatabases()) {
                MountedWublastBlastService.getInstance().regenerateCuratedDatabases();
            }
        } catch (BlastDatabaseException e) {
            logger.error("failed to validate curated database", e);
            String reportName = jobName + ".errors";
            ReportGenerator rg = new ReportGenerator();
            rg.setReportTitle("Report for " + jobName);
            rg.includeTimestamp();
            rg.addErrorMessage("Failed to validate curated databases");
            rg.addErrorMessage(e);
            rg.writeFiles(new File(dataDirectory, jobName), reportName);
            exitCode = 1;
        } finally {
            HibernateUtil.closeSession();
        }
        return exitCode;
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        RegenerateWebHostCuratedDatabasesJob job = new RegenerateWebHostCuratedDatabasesJob();
        job.setPropertyFilePath(args[0]);
        job.init(args[1]);
        job.setJobName(args[2]);
        job.init();
        System.exit(job.execute());
    }
}
