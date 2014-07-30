package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;

import java.util.List;

/**
 */
public class ValidateBlastDatabases extends AbstractValidateDataReportTask {

    private static Logger logger = Logger.getLogger(ValidateBlastDatabases.class);

    @Override
    public void execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        List<String> failures = MountedWublastBlastService.getInstance().validateAllPhysicalDatabasesReadable();
        if (CollectionUtils.isNotEmpty(failures)) {
            String reportName = jobName + ".errors";
            ReportConfiguration config = new ReportConfiguration(jobName, dataDirectory, reportName, true);
            createErrorReport(null, getStringifiedList(failures), config);
        } else {
            logger.info("No failed databases found.");
        }
        HibernateUtil.closeSession();
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        ValidateBlastDatabases job = new ValidateBlastDatabases();
        job.setPropertyFilePath(args[0]);
        job.setBaseDir(args[1]);
        job.setJobName(args[2]);
        job.init();
        job.execute();
    }

}