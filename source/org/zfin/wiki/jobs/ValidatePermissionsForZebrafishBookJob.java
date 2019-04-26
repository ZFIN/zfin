package org.zfin.wiki.jobs;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.wiki.*;
import org.zfin.wiki.service.AntibodyWikiWebService;
import org.zfin.wiki.service.WikiWebService;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class makes sure that every page with the label "zebrafish_book" can be edited by "zfin-users".
 */
public class ValidatePermissionsForZebrafishBookJob extends AbstractValidateDataReportTask {

    private static final Logger logger = LogManager.getLogger(ValidatePermissionsForZebrafishBookJob.class);

    public ValidatePermissionsForZebrafishBookJob(String jobName, String propertyFilePath, String jobDirectoryString) {
        super(jobName, propertyFilePath, jobDirectoryString);
    }

    @Override
    public int execute() {
        try {
            List<String> pagesWithBadPermissions = new ArrayList<>();
            WikiWebService webService = WikiWebService.getInstance();
            RemoteSearchResult[] results = webService.getLabelContent(Label.ZEBRAFISH_BOOK.getValue());
            for (RemoteSearchResult result : results) {
                // skip if page is deleted
                if (!webService.getPage(result.getId()).isCurrent())
                    continue;
                try {
                    boolean isZfinGroup = false;
                    RemoteContentPermission[] remoteContentPermissions = webService.getRemoteContentPermissions(result.getId(), Permission.EDIT.getValue());
                    for (RemoteContentPermission remoteContentPermission : remoteContentPermissions) {
                        String groupName = remoteContentPermission.getGroupName();
                        if (groupName == null)
                            continue;
                        if (groupName.equals(Group.ZFIN_USERS.getValue())) {
                            isZfinGroup = true;
                        }
                    }
                    if (!isZfinGroup) {
                        pagesWithBadPermissions.add(result.getTitle());
                    }

                } catch (RemoteException e) {
                    logger.error("failed to evaluate permission for: " + result.getTitle(), e);
                }
            }

            // send report
            clearReportDirectory();
            setReportProperties();
            createReport(pagesWithBadPermissions);
            return pagesWithBadPermissions.size();
        } catch (Exception e) {
            logger.error("Failed out validate permissions for zebrafish book job", e);
            return 1;
        }
    }

    private void createReport(List<String> pages) {
        if (CollectionUtils.isEmpty(pages)) {
            logger.info("no zebrafish book pages detected with bad permissions");
            return;
        }
        String templateName = jobName + ".faulty-pages";
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, templateName, true);
        createErrorReport(null, getStringifiedList(pages), reportConfiguration);
        logger.info(pages);
    }


    public static void main(String[] args) throws WikiLoginException, FileNotFoundException, InterruptedException {
        initLog4J();
        setLoggerToInfoLevel(logger);
        setLoggerToInfoLevel(AbstractValidateDataReportTask.LOG);
        setLoggerToInfoLevel(AntibodyWikiWebService.logger);
        setLoggerToInfoLevel(WikiWebService.logger);
        logger.info("Setting Permissions on Zebrafish Book pages...");
        String propertyFilePath = args[0];
        String jobDirectoryString = args[1];
        ValidatePermissionsForZebrafishBookJob job =
                new ValidatePermissionsForZebrafishBookJob(args[2], propertyFilePath, jobDirectoryString);
        job.setLoggerFile();
        System.exit(job.execute());
    }

}
