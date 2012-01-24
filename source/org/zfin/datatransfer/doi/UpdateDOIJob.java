package org.zfin.datatransfer.doi;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.Date;

/**
 */
public class UpdateDOIJob extends ZfinBasicQuartzJob {

    private Logger logger = Logger.getLogger(UpdateDOIJob.class) ;

    // defaults are for monthly scheduling
    private boolean reportAll = true;
    private int maxToProcess = DOIProcessor.ALL ;
    private int maxAttempts = DOIProcessor.ALL ;

    public void run(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            DOIProcessor driver = new DOIProcessor(reportAll,maxAttempts,maxToProcess);
            driver.findAndUpdateDOIs();

            if (reportAll == true || driver.isDoisUpdated()) {
                (new IntegratedJavaMailSender()).sendHtmlMail("doi updates for: " + (new Date()).toString()
                        , driver.getMessage().toString().replaceAll("\n","<br>\n"), ZfinProperties.splitValues(ZfinPropertiesEnum.DOI_EMAIL_REPORT));
            }
        }
        catch (Exception e) {
            logger.error("Failed to update DOI Job",e);
        }
    }

    public boolean isReportAll() {
        return reportAll;
    }

    public void setReportAll(boolean reportAll) {
        this.reportAll = reportAll;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getMaxToProcess() {
        return maxToProcess;
    }

    public void setMaxToProcess(int maxToProcess) {
        this.maxToProcess = maxToProcess;
    }
}
