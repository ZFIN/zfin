package org.zfin.fish.quartz;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.DateUtil;

/**
 * Load obo file for ontology into the database.
 */
public class CheckGenoxMaxIdsLengthJob extends ZfinBasicQuartzJob implements StatefulJob {

    private Logger log = Logger.getLogger("org.zfin.ontology");
    private String jobName;
    private static final int MAX_GENOX_LENGTH = 7000;

    public void run(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        long startTime = System.currentTimeMillis();
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        CronJobReport report = new CronJobReport(jobName, cronJobUtil);
        report.start();
        try {
            String genoxGroupId = FishService.getGenoxMaxLength();
            if (genoxGroupId.length() > MAX_GENOX_LENGTH) {
                String message = "The maximum genotype experiment id length is exceeded for fas_genox_group = " + genoxGroupId;
                message += "\n Could cause problems for the URLs off the fish search result page as this is using (genoID, genoxIds) as" +
                        " a primary key";
                report.error(message);
            } else
                report.addMessageToSection("Maximum length of fas_genotype_group is: " + genoxGroupId.length(), "validate");
                report.addMessageToSection("fas_genotype_group: " + genoxGroupId, "validate");
        } catch (Exception e) {
            String message = "Error while loading the ontologies into the database";
            report.error(message);
            report.error(e);
            log.error(message, e);
        }
        report.finish();
        log.info("Duration: " + DateUtil.getTimeDuration(startTime));
        // send main email report
        cronJobUtil.emailReport("max-genox-ids-length-report.ftl", report, null);
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
