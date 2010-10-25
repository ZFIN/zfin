package org.zfin.ontology.quartz;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.DateUtil;

/**
 * Re-load the ontologies into memory.
 */
public class ReloadOntologiesJob extends QuartzJobBean implements StatefulJob {

    private static final Logger LOG = Logger.getLogger(ReloadOntologiesJob.class);
    private String jobName;

    public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        CronJobReport report = new CronJobReport(jobName);
        report.start();
        long startTime = System.currentTimeMillis();
        try {
            OntologyManager ontologyManager = OntologyManager.getInstance();
            ontologyManager.reLoadOntologies();
        } catch (Exception e) {
            LOG.error("Error while reloading the ontologies into Java Memory", e);
        }
        String duration = DateUtil.getTimeDuration(startTime);
        report.finish();
        CronJobUtil util = new CronJobUtil(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL.value());
        util.emailReport("ontology-loader-reload-into-memory.ftl", report, null);
        LOG.info("Finished re-loading all ontologies in Quartz Job: Duration " + duration);
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
