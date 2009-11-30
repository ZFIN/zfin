package org.zfin.datatransfer;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.properties.ZfinProperties;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;

/**
 * Job to update DOI's.
 */
public class UpdateDOIJob extends QuartzJobBean {

    private boolean reportAll = false;

    public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            UpdateDOIMain driver = new UpdateDOIMain(reportAll);
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
}
