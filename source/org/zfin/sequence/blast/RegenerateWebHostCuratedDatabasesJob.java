package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;

/**
 */
public class RegenerateWebHostCuratedDatabasesJob extends ZfinBasicQuartzJob {

    private Logger logger = Logger.getLogger(RegenerateWebHostCuratedDatabasesJob.class);

    protected void run(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("validating curated webhost database");
        try {
            if (MountedWublastBlastService.getInstance().validateCuratedDatabases()) {
                MountedWublastBlastService.getInstance().regenerateCuratedDatabases();
            }
//            (new IntegratedJavaMailSender()).sendMail("Validate curated databases",
//                    "Validate curated databases." , ZfinProperties.getValidationOtherEmailAddresses());
        } catch (BlastDatabaseException e) {
            logger.error("failed to validate curated database", e);
            (new IntegratedJavaMailSender()).sendMail("Failed to validate curated databases",
                    "Failed to validate curated databases:" +
                            "\n" + e, ZfinProperties.getValidationOtherEmailAddresses());
        }
        HibernateUtil.closeSession();
    }
}
