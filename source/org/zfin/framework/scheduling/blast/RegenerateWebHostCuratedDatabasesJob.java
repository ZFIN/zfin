package org.zfin.framework.scheduling.blast;

import org.springframework.scheduling.quartz.QuartzJobBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.apache.log4j.Logger;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.sequence.blast.BlastDatabaseException;
import org.zfin.sequence.blast.MountedWublastBlastService;

/**
 */
public class RegenerateWebHostCuratedDatabasesJob extends QuartzJobBean {

    private Logger logger  = Logger.getLogger(RegenerateWebHostCuratedDatabasesJob.class) ;
    
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("validating curated webhost database");
        try {
            MountedWublastBlastService.getInstance().validateCuratedDatabases();
//            (new IntegratedJavaMailSender()).sendMail("Validate curated databases",
//                    "Validate curated databases." , ZfinProperties.getValidationOtherEmailAddresses());
        } catch (BlastDatabaseException e) {
            logger.error("failed to validate curated database",e);
            (new IntegratedJavaMailSender()).sendMail("Failed to validate curated databases",
                    "Failed to validate curated databases:" +
                    "\n" + e , ZfinProperties.getValidationOtherEmailAddresses());
        }
        HibernateUtil.closeSession();
    }
}
