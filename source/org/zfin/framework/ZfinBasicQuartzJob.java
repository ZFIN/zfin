package org.zfin.framework;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.repository.RepositoryFactory;

/**
 * Always extend this class to ensure that jobs are honoring the update=disable flag.
 */
public abstract class ZfinBasicQuartzJob extends QuartzJobBean {

    private Logger log = Logger.getLogger(ZfinBasicQuartzJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // check if the flag is set. If so do not continue the job.
        boolean systemUpdatesDisabled = RepositoryFactory.getInfrastructureRepository().getDisableUpdatesFlag();
        if (systemUpdatesDisabled)
            log.warn("Updates are disabled! Quartz job is not being executed.");
        else
            run(jobExecutionContext);
    }

    abstract protected void run(JobExecutionContext jobExecutionContext) throws JobExecutionException;
}
