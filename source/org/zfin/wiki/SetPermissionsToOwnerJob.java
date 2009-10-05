package org.zfin.wiki;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.properties.ZfinProperties;

import javax.xml.rpc.ServiceException;

/**
 */
public class SetPermissionsToOwnerJob implements Job {

    private final Logger logger = Logger.getLogger(SetPermissionsToOwnerJob.class) ;

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            if(ZfinProperties.isPushToWiki()){
                WikiWebService wikiService = WikiWebService.getInstance();
                wikiService.setOwnerForLabel("community_protocol") ;
                wikiService.setOwnerForLabel("community_antibody") ;
                wikiService.setOwnerForLabel("community_antibody_procedure") ;
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}