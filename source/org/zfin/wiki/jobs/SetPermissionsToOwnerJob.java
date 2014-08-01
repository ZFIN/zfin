package org.zfin.wiki.jobs;

import org.apache.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.properties.ZfinProperties;
import org.zfin.wiki.service.WikiWebService;

import java.util.ArrayList;
import java.util.List;

/**
 * Set permission of pages with the following labels to the original creator if does not already
 * have an owner.
 */
public class SetPermissionsToOwnerJob extends AbstractValidateDataReportTask {

    private static final Logger logger = Logger.getLogger(SetPermissionsToOwnerJob.class);

    @Override
    public void execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        try {
            if (ZfinProperties.isPushToWiki()) {
                WikiWebService wikiService = WikiWebService.getInstance();
                List<String> pages = new ArrayList<>();
                pages.addAll(wikiService.setOwnerForLabel("community_protocol"));
                pages.addAll(wikiService.setOwnerForLabel("community_antibody"));

                if (pages.size() > 0) {
                    ReportConfiguration config = new ReportConfiguration(jobName, dataDirectory, jobName, true);
                    createErrorReport(null, getStringifiedList(pages), config);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while setting wiki page permissions", e);
        }
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        setLoggerToInfoLevel(WikiWebService.logger);
        SetPermissionsToOwnerJob job = new SetPermissionsToOwnerJob();
        job.setPropertyFilePath(args[0]);
        job.setBaseDir(args[1]);
        job.setJobName(args[2]);
        job.init(false);
        job.execute();
    }
}