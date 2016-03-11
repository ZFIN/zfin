package org.zfin.ontology.jobs;

import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.EapQualityTermDTO;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.ontology.Term;
import org.zfin.util.ReportGenerator;
import org.zfin.wiki.WikiLoginException;
import org.zfin.wiki.WikiSynchronizationReport;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 */
public class CheckEapPatoTermsJob extends AbstractValidateDataReportTask {

    private static final Logger logger = Logger.getLogger(CheckEapPatoTermsJob.class);

    public CheckEapPatoTermsJob(String jobName, String propertyFilePath, String dataDirectoryString) {
        super(jobName, propertyFilePath, dataDirectoryString);
    }

    public static void main(String[] args) throws WikiLoginException, FileNotFoundException, InterruptedException {
        initLog4J();
        setLoggerToInfoLevel(logger);
        logger.info("Checking EaP PATO terms for obsoletion and merging");
        String propertyFilePath = args[0];
        String jobDirectoryString = args[1];
        CheckEapPatoTermsJob job = new CheckEapPatoTermsJob(args[2], propertyFilePath, jobDirectoryString);
        job.initDatabase(true);
        System.exit(job.execute());
    }

    private void createReportFiles(WikiSynchronizationReport report) {
        if (report == null) {
            return;
        }
        String reportName = jobName + ".updated-antibodies";
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, getStringifiedList(report.getUpdatedPages()), reportConfiguration);

        reportName = jobName + ".created-antibodies";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, getStringifiedList(report.getCreatedPages()), reportConfiguration);

        reportName = jobName + ".dropped-antibodies";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, getStringifiedList(report.getDroppedPages()), reportConfiguration);

        System.out.print(report);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();
        int exitCode = 0;

        List<List<String>> obsoleteTermList = new ArrayList<>(2);
        List<List<String>> mergedTermList = new ArrayList<>(2);
        for (String patoString : EapQualityTermDTO.nicknameMap.keySet()) {
            String id = patoString.split(",")[0];
            Term patoTerm = getOntologyRepository().getTermByOboID(id);
            List<String> list = new ArrayList<>(2);
            if (patoTerm == null) {
                list.add(patoString);
                mergedTermList.add(list);
                continue;
            }
            if (patoTerm.isObsolete()) {
                list.add(patoTerm.getOboID());
                list.add(patoTerm.getTermName());
            }
            obsoleteTermList.add(list);
        }

        String reportName = jobName + ".obsoleted";
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, obsoleteTermList, reportConfiguration);

        reportName = jobName + ".merged";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, mergedTermList, reportConfiguration);

        ReportGenerator statistics = new ReportGenerator();
        Map<String, Object> summary = new HashMap<>();
/*
        summary.put("Created Wiki Pages", report.getCreatedPages().size());
        summary.put("Updated Wiki Pages", report.getUpdatedPages().size());
        summary.put("Dropped Wiki Pages", report.getDroppedPages().size());
*/
        statistics.setReportTitle("Report for " + jobName);
        statistics.includeTimestamp();
        statistics.addSummaryTable(summary);
        statistics.writeFiles(dataDirectory, jobName + ".statistics");
        return exitCode;
    }
}
