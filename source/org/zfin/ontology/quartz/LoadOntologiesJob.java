package org.zfin.ontology.quartz;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.zfin.framework.ZfinBasicQuartzJob;
import org.zfin.ontology.datatransfer.CronJobReport;
import org.zfin.ontology.datatransfer.CronJobUtil;
import org.zfin.ontology.datatransfer.DownloadOntology;
import org.zfin.ontology.datatransfer.service.LoadOntology;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.DateUtil;
import org.zfin.util.FileUtil;
import org.zfin.util.LoggingUtil;

import java.io.File;

/**
 * Load obo file for ontology into the database.
 */
public class LoadOntologiesJob extends ZfinBasicQuartzJob implements StatefulJob {

    private Logger log = Logger.getLogger("org.zfin.ontology");
    private static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    private String oboFileName;
    private String oboFileUrl;
    private String scriptDirectory;
    private String scriptFileName;
    private boolean useExistingFile;
    private String jobName;

    public void run(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Appender appender = LoggingUtil.addFileAppender(log, "load-ontology-" + oboFileName);
        long startTime = System.currentTimeMillis();
        String duration = null;
        CronJobUtil cronJobUtil = new CronJobUtil(ZfinProperties.splitValues(ZfinPropertiesEnum.ONTOLOGY_LOADER_EMAIL));
        CronJobReport report = new CronJobReport(jobName, cronJobUtil);
        report.start();
        try {
            String localFileName = FileUtil.createAbsolutePath(TEMP_DIRECTORY, oboFileName);
            if (!useExistingFile) {
                log.info("Start downloading ontology file: " + localFileName);
                DownloadOntology download = new DownloadOntology(localFileName, oboFileUrl);
                download.downloadOntology();
                log.info("Finished downloading ontology file: " + localFileName);
                duration = DateUtil.getTimeDuration(startTime);
                log.info("Duration: " + duration);
            } else {
                log.info("Starting to reuse obo file (no fresh download): " + localFileName);
            }
            File localOboFile = new File(localFileName);
            if (!localOboFile.exists()) {
                report.error("Could not find local obo file: "+localFileName);
            } else {
                String scriptDirectoryFullPath = FileUtil.createAbsolutePath(ZfinPropertiesEnum.WEBROOT_DIRECTORY.value(), scriptDirectory);
                String[] dbScriptFiles = scriptFileName.split(",");
                int index = 0;
                // prefix the full path to the file names.
                for (String fileName : dbScriptFiles)
                    dbScriptFiles[index++] = FileUtil.createAbsolutePath(scriptDirectoryFullPath, fileName);
                LoadOntology loader = new LoadOntology(localFileName, dbScriptFiles);
                loader.setLogger(log);
                if (loader.initialize(report))
                    loader.getReport().addMessageToSection("Downloaded Obo file " + oboFileUrl, "Load Obo file");
                loader.getReport().addMessageToSection("Duration: " + duration, "Load Obo file");
                loader.runOntologyUpdateProcess();
            }
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
        LoggingUtil.removeAppender(log, appender);
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
