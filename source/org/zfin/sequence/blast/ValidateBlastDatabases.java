package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;

import java.util.List;

/**
 */
public class ValidateBlastDatabases extends AbstractValidateDataReportTask {

    private static Logger logger = LogManager.getLogger(ValidateBlastDatabases.class);

    public ValidateBlastDatabases(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @Override
    public int execute() {
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
        return failures.size();
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        ValidateBlastDatabases job = new ValidateBlastDatabases(args[2], args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
    }

}