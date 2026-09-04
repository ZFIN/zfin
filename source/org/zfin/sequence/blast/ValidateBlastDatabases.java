package org.zfin.sequence.blast;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;

import java.util.Comparator;
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

        BlastDatabaseStalenessPolicy stalenessPolicy = BlastDatabaseStalenessPolicy.fromProperties(reportProperties, jobName);
        logger.info("staleness policy: default " + stalenessPolicy.defaultMaxAgeDays() + " days, "
                    + stalenessPolicy.overrides().size() + " override(s), "
                    + stalenessPolicy.exempt().size() + " exempt database(s)");

        List<BlastDatabaseValidationFinding> findings = MountedWublastBlastService.getInstance().validatePhysicalDatabases(stalenessPolicy);
        if (CollectionUtils.isNotEmpty(findings)) {
            // Worst first, so a database that has gone missing outranks one that is
            // merely overdue for a rebuild.
            findings.sort(Comparator.comparing((BlastDatabaseValidationFinding finding) -> finding.problem().ordinal())
                    .thenComparing(BlastDatabaseValidationFinding::abbrev));
            for (BlastDatabaseValidationFinding finding : findings) {
                logger.error(finding);
            }
            String reportName = jobName + ".errors";
            ReportConfiguration config = new ReportConfiguration(jobName, dataDirectory, reportName, true);
            createErrorReport(null, findings.stream().map(BlastDatabaseValidationFinding::toReportRow).toList(), config);
        } else {
            logger.info("No failed databases found.");
        }
        HibernateUtil.closeSession();
        return findings.size();
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        ValidateBlastDatabases job = new ValidateBlastDatabases(args[2], args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
    }

}
