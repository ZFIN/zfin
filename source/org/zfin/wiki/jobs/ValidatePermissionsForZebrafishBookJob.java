package org.zfin.wiki.jobs;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;
import org.zfin.wiki.*;
import org.zfin.wiki.service.WikiWebService;

import java.util.ArrayList;
import java.util.List;

/**
 * This class makes sure that every page with the label "zebrafish_book" can be edited by "zfin-users".
 */
public class ValidatePermissionsForZebrafishBookJob extends ZfinBasicQuartzJob {

    private final Logger logger = Logger.getLogger(ValidatePermissionsForZebrafishBookJob.class);

    @Override
    public void run(JobExecutionContext context) throws JobExecutionException {

        try{
            List<String> pagesWithBadPermissions = new ArrayList<String>() ;
            RemoteSearchResult[] results = WikiWebService.getInstance().getLabelContent(Label.ZEBRAFISH_BOOK.getValue()) ;
            for(RemoteSearchResult result : results){
                try{
                    boolean isZfinGroup = false ;
                    RemoteContentPermission[] remoteContentPermissions = WikiWebService.getInstance().getRemoteContentPermissions(result.getId(), Permission.EDIT.getValue()) ;
                    for(RemoteContentPermission remoteContentPermission: remoteContentPermissions){
                        String groupName = remoteContentPermission.getGroupName() ;
                        if(groupName.equals(Group.ZFIN_USERS.getValue())){
                            isZfinGroup = true ;
                        }
                    }
                    if(!isZfinGroup){
                        pagesWithBadPermissions.add(result.getTitle()) ;
                    }

                } catch (RemoteException e) {
                    logger.error("failed to evaluate permission for: " + result.getTitle(),e) ;
                }
            }

            // send report

            if(pagesWithBadPermissions.size()>0){
                logger.error("# of bad pages: "+ pagesWithBadPermissions.size() + " of " + results.length) ;
                StringBuffer stringBuffer = new StringBuffer() ;
                for(String badPage: pagesWithBadPermissions){
                    stringBuffer.append(badPage).append("\n") ;
                }

                (new IntegratedJavaMailSender()).sendMail("Zebrafish book pages have bad permissions"
                        , pagesWithBadPermissions.size()+" pages have bad zebrafish book permission:\n"+stringBuffer.toString(), ZfinProperties.getValidationOtherEmailAddresses());

                logger.info("mail sent") ;
            }
            else{
                logger.info("no zebrafish book pages detected with bad permissions");
            }


        }
        catch(Exception e){
            logger.error("Failed out validate permssions for zebrafish book job",e);
        }
    }
    
}
