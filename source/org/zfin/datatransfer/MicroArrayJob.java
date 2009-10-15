package org.zfin.datatransfer;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;

import java.util.Date;

/**
 */
public class MicroArrayJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            UpdateMicroArrayMain main = new UpdateMicroArrayMain() ;
            main.init() ;
            main.run() ;
            (new IntegratedJavaMailSender()).sendMail("microarray updates for: "+(new Date()).toString()
                    , main.getMicroArrayBean().toString(), ZfinProperties.getValidationOtherEmailAddresses());
        }
        catch(Exception e){
            // the error should already be logged
            e.printStackTrace() ;
        }

    }

}
