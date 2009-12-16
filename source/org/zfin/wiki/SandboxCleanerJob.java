package org.zfin.wiki;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Cleans sandbox.
 */
public class SandboxCleanerJob extends QuartzJobBean {

    private final Logger logger = Logger.getLogger(SandboxCleanerJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            WikiWebService.getInstance().cleanSandbox() ;
        } catch (Exception e) {
            logger.error("Failed to clean sandbox",e);
            throw new JobExecutionException(e);
        }

    }
}
