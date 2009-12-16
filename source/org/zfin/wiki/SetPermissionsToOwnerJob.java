package org.zfin.wiki;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.properties.ZfinProperties;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Set permission of pages with the following labels to the original creator if does not already
 * have an owner.
 */
public class SetPermissionsToOwnerJob extends QuartzJobBean {

    private final Logger logger = Logger.getLogger(SetPermissionsToOwnerJob.class);

    public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (ZfinProperties.isPushToWiki()) {
                WikiWebService wikiService = WikiWebService.getInstance();
                wikiService.setOwnerForLabel("community_protocol");
                wikiService.setOwnerForLabel("community_antibody");
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}