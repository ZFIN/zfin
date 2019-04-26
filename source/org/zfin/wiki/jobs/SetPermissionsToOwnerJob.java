package org.zfin.wiki.jobs;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.properties.ZfinProperties;
import org.zfin.wiki.service.WikiWebService;

import java.util.ArrayList;
import java.util.List;

/**
 * Set permission of pages with the following labels to the original creator if does not already
 * have an owner.
 */
public class SetPermissionsToOwnerJob extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(SetPermissionsToOwnerJob.class);

    public SetPermissionsToOwnerJob(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @Override
    public int execute() {
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
                    createErrorReport(null, getStringifiedList(pages));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while setting wiki page permissions", e);
        }
        return 0;
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        setLoggerToInfoLevel(WikiWebService.logger);
        SetPermissionsToOwnerJob job = new SetPermissionsToOwnerJob(args[2], args[0], args[1]);
        System.exit(job.execute());
    }
}