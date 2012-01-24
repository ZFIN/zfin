package org.zfin.wiki.jobs;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.wiki.service.AntibodyWikiWebService;

/**
 */
public class AntibodyWikiSynchronizationJob extends ZfinBasicQuartzJob {

    private final Logger logger = Logger.getLogger(AntibodyWikiSynchronizationJob.class);

    public void run(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("pushing antibodies to antibody wiki");
        try {
            AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
        } catch (Exception e) {
            logger.error("Failed to synchronize ZFIN antibodies with wiki antibodies",e);
            throw new JobExecutionException(e);
        }
    }

}
