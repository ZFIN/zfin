package org.zfin.datatransfer.microarray ;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.Date;

/**
 */
public class MicroarrayJob implements Job {

    private Logger logger = Logger.getLogger(MicroarrayJob.class) ;

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try{
            MicroarrayProcessor processor = new MicroarrayProcessor() ;
            processor.init() ;
            MicroarrayBean microarrayBean = processor.run() ;
            (new IntegratedJavaMailSender()).sendMail("microarray updates for: "+(new Date()).toString()
                    , microarrayBean.finishReadingAndRetrieve(),
                    ZfinProperties.splitValues(ZfinPropertiesEnum.MICROARRAY_EMAIL));
        }
        catch(Exception e){
            // the error should already be logged
            logger.error(e);
        }

    }

}
