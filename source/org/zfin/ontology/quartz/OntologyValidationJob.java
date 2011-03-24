package org.zfin.ontology.quartz;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.ontology.datatransfer.DownloadOntology;
import org.zfin.ontology.datatransfer.LoadOntology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.DateUtil;
import org.zfin.util.FileUtil;
import org.zfin.util.LoggingUtil;

import java.io.File;

/**
 * Load obo file for ontology into the database.
 */
public class OntologyValidationJob extends QuartzJobBean implements StatefulJob {

    private Logger log = Logger.getLogger("org.zfin.ontology");
    private static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    private String oboFileName;
    private String oboFileUrl;
    private String scriptDirectory;
    private String scriptFileName;
    private boolean useExistingFile;
    private String jobName;

    public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        long startTime = System.currentTimeMillis();
        String duration = null;
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        CronJobReport report = new CronJobReport(jobName, cronJobUtil);
        report.start();
        try {
            OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
            //ontologyRepository.getObsoletedTermsUsedInAnnotations();
        } catch (Exception e) {
            String message = "Error while loading the ontologies into the database";
            report.error(message);
            report.error(e);
            log.error(message, e);
        }
        report.finish();
        log.info("Finished loading ontology " + oboFileName + " in Quartz Job: Took " + DateUtil.getTimeDuration(startTime));
        log.info("Duration: " + DateUtil.getTimeDuration(startTime));
        String filename = ((FileAppender) log.getAppender("ontology-logger")).getFile();
        // send main email report 
        cronJobUtil.emailReport("ontology-loader-report.ftl", report, filename);
    }

    public void setOboFileName(String oboFileName) {
        this.oboFileName = oboFileName;
    }

    public void setOboFileUrl(String oboFileUrl) {
        this.oboFileUrl = oboFileUrl;
    }

    public void setScriptDirectory(String scriptDirectory) {
        this.scriptDirectory = scriptDirectory;
    }

    public void setScriptFileName(String scriptFileName) {
        this.scriptFileName = scriptFileName;
    }

    public String getOboFileName() {
        return oboFileName;
    }

    public String getOboFileUrl() {
        return oboFileUrl;
    }

    public String getScriptDirectory() {
        return scriptDirectory;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public boolean isUseExistingFile() {
        return useExistingFile;
    }

    public void setUseExistingFile(boolean useExistingFile) {
        this.useExistingFile = useExistingFile;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
