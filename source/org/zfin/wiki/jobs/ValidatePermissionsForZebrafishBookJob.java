package org.zfin.wiki.jobs;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.properties.ZfinProperties;
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

    private static final Logger logger = Logger.getLogger(ValidatePermissionsForZebrafishBookJob.class);

    @Override
    public void execute() {
        try {
            List<String> pagesWithBadPermissions = new ArrayList<>();
            RemoteSearchResult[] results = WikiWebService.getInstance().getLabelContent(Label.ZEBRAFISH_BOOK.getValue());
            for (RemoteSearchResult result : results) {
                try {
                    boolean isZfinGroup = false;
                    RemoteContentPermission[] remoteContentPermissions = WikiWebService.getInstance().getRemoteContentPermissions(result.getId(), Permission.EDIT.getValue());
                    for (RemoteContentPermission remoteContentPermission : remoteContentPermissions) {
                        String groupName = remoteContentPermission.getGroupName();
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

            createReport(pagesWithBadPermissions);
        } catch (Exception e) {
            logger.error("Failed out validate permissions for zebrafish book job", e);
        }
    }

    private void createReport(List<String> pages) {
        if (CollectionUtils.isEmpty(pages)) {
            logger.info("no zebrafish book pages detected with bad permissions");
            return;
        }
        createErrorReport(null, getStringifiedList(pages), "faulty-pages");
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
        ValidatePermissionsForZebrafishBookJob job = new ValidatePermissionsForZebrafishBookJob();
        job.setPropertyFilePath(propertyFilePath);
        job.setBaseDir(jobDirectoryString);
        job.setJobName(args[2]);
        job.init(false);
        job.execute();
    }

}
