package org.zfin.curation.presentation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.profile.Person;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.util.ReportGenerator;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.repository.RepositoryFactory.getProfileRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 *
 */
public class StudentMetricsJob extends AbstractValidateDataReportTask {

    private static Logger logger = LogManager.getLogger(StudentMetricsJob.class);

    public StudentMetricsJob(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();
        runLoad();
        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.includeTimestamp();
        return 0;
    }

    private void runLoad() {
        try {
            HibernateUtil.createTransaction();
            runReport();
            HibernateUtil.flushAndCommitCurrentSession();
            LOG.info("Committed load...");
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    private void runReport() {
        Person jaime = getProfileRepository().getPerson("ZDB-PERS-220425-1");
        Person kaia = getProfileRepository().getPerson("ZDB-PERS-201217-1");
        PublicationTrackingStatus readyForIndexing = getPublicationRepository().getPublicationTrackingStatus(2);
        PublicationTrackingStatus manualPdfAcquisition = getPublicationRepository().getPublicationTrackingStatus(22);
        List<PublicationTrackingStatus> allStatused = getPublicationRepository().getAllPublicationStatuses();
        List<PublicationTrackingStatus> closed = allStatused.stream()
                .filter(stat -> stat.getType().equals(PublicationTrackingStatus.Type.CLOSED))
                .collect(Collectors.toList());

        ReportGenerator stats = new ReportGenerator();
        stats.setReportTitle("Report for " + jobName);
        stats.includeTimestamp();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Stats for Jaime","");
        summary.put("Number of Pubs moved to Ready-For-Indexing",
                getPublicationRepository().getPublicationTrackingStatus(jaime, 48, readyForIndexing));
        summary.put("number of pubs transitioned to ‘manual pdf acquisition",
                getPublicationRepository().getPublicationTrackingStatus(jaime, 48, manualPdfAcquisition));
        summary.put("number of pubs closed",
                getPublicationRepository().getPublicationTrackingStatus(jaime, 48, closed.toArray(PublicationTrackingStatus[]::new)));
        summary.put("","");
        summary.put("Stats for Kaia","");
        summary.put("Kaia: Number of Pubs moved to Ready-For-Indexing",
                getPublicationRepository().getPublicationTrackingStatus(kaia, 48, readyForIndexing));
        summary.put("Kaia: number of pubs transitioned to ‘manual pdf acquisition",
                getPublicationRepository().getPublicationTrackingStatus(kaia, 48, manualPdfAcquisition));
        summary.put("Kaia: number of pubs closed",
                getPublicationRepository().getPublicationTrackingStatus(kaia, 48, closed.toArray(PublicationTrackingStatus[]::new)));
        stats.addSummaryTable("Statistics", summary);
        stats.writeFiles(new File(dataDirectory, jobName), "statistics");
    }


    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        String jobName = args[2];
        StudentMetricsJob job = new StudentMetricsJob(jobName, args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
    }
}
