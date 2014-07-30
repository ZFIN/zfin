package org.zfin.sequence.blast;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 */
public class RegenerateWebHostCuratedDatabasesJob extends AbstractValidateDataReportTask {

    private static Logger logger = Logger.getLogger(RegenerateWebHostCuratedDatabasesJob.class);

    @Override
    public void execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        logger.info("validating curated webhost database");
        try {
            if (MountedWublastBlastService.getInstance().validateCuratedDatabases()) {
                MountedWublastBlastService.getInstance().regenerateCuratedDatabases();
            }
        } catch (BlastDatabaseException e) {
            logger.error("failed to validate curated database", e);
            List<String> failure = Arrays.asList(ExceptionUtils.getFullStackTrace(e));
            String reportName = jobName + ".errors";
            ReportConfiguration config = new ReportConfiguration(jobName, dataDirectory, reportName, true);
            createErrorReport(null, getStringifiedList(failure), config);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        RegenerateWebHostCuratedDatabasesJob job = new RegenerateWebHostCuratedDatabasesJob();
        job.setPropertyFilePath(args[0]);
        job.setBaseDir(args[1]);
        job.setJobName(args[2]);
        job.init();
        job.execute();
    }
}
