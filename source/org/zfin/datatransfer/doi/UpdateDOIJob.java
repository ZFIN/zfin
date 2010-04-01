package org.zfin.datatransfer.doi;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.properties.ZfinProperties;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 */
public class UpdateDOIJob extends QuartzJobBean {

    // defaults are for monthly scheduling
    private boolean reportAll = true;
    private int maxToProcess = DOIProcessor.ALL ;
    private int maxAttempts = DOIProcessor.ALL ;

    public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            DOIProcessor driver = new DOIProcessor(reportAll,maxAttempts,maxToProcess);
            driver.findAndUpdateDOIs();

            if (reportAll == true || driver.isDoisUpdated()) {
                (new IntegratedJavaMailSender()).sendMail("doi updates for: " + (new Date()).toString()
                        , driver.getMessage().toString(), ZfinProperties.getValidationOtherEmailAddresses());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
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
