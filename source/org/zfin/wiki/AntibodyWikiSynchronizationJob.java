package org.zfin.wiki;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 */
public class AntibodyWikiSynchronizationJob extends QuartzJobBean {

    private final Logger logger = Logger.getLogger(AntibodyWikiSynchronizationJob.class);

    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("pushing antibodies to antibody wiki");
        try {
            AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
        } catch (Exception e) {
            logger.error("Failed to synchronize ZFIN antibodies with wiki antibodies",e);
            throw new JobExecutionException(e);
        }
    }

}
