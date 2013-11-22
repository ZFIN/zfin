package org.zfin.wiki.jobs;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.properties.ZfinProperties;
import org.zfin.wiki.service.WikiWebService;

/**
 * Cleans sandbox.
 */
public class SandboxCleanerJob extends ZfinBasicQuartzJob {

    private final Logger logger = Logger.getLogger(SandboxCleanerJob.class);

    @Override
    public void run(JobExecutionContext context) throws JobExecutionException {
        try {
            WikiWebService.getInstance().cleanSandbox() ;
        } catch (Exception e) {
            logger.error("Failed to clean sandbox",e);
            throw new JobExecutionException(e);
        }
    }

    public static void main(String[] arguments) throws Exception {
        ZfinProperties.init(arguments[0]);
        WikiWebService.getInstance().cleanSandbox() ;
    }
}
