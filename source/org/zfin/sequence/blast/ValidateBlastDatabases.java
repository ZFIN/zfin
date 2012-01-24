package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;

import java.util.List;

/**
 */
public class ValidateBlastDatabases extends ZfinBasicQuartzJob {

    private Logger logger = Logger.getLogger(ValidateBlastDatabases.class);

    public void validateDatabase() {
        List<String> failures = MountedWublastBlastService.getInstance().validateAllPhysicalDatabasesReadable();
        if (CollectionUtils.isNotEmpty(failures)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to validate remote databases:\n");
            for (String failure : failures) {
                logger.error(failure);
                sb.append(failure).append("\n");
            }
            (new IntegratedJavaMailSender()).sendMail("Failed to validate " + failures.size() + " remote databases",
                    sb.toString(), ZfinProperties.getValidationOtherEmailAddresses());
        } else {
            logger.info("No failed databases found.");
        }
        HibernateUtil.closeSession();

    }

    public void run(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        validateDatabase();
    }

}