package org.zfin.wiki;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 */
public class SynchronizeZFINAntibodiesWithWikiJob extends QuartzJobBean {

    private final Logger logger = Logger.getLogger(SynchronizeZFINAntibodiesWithWikiJob.class);

    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("pushing antibodies to antibody wiki");
        try {
            AntibodyWikiWebService.getInstance().synchronizeAntibodiesOnWikiWithZFIN();
        } catch (WikiLoginException e) {
            logger.error(e);
            throw new JobExecutionException(e);
        }
    }

}
