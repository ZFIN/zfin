package org.zfin.ontology.jobs;

import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.EapQualityTermDTO;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.infrastructure.ant.ReportConfiguration;
import org.zfin.ontology.Term;
import org.zfin.wiki.WikiLoginException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();

        List<List<String>> obsoleteTermList = new ArrayList<>(2);
        List<List<String>> mergedTermList = new ArrayList<>(2);
        for (String patoString : EapQualityTermDTO.nicknameMap.keySet()) {
            String id = patoString.split(",")[0];
            Term patoTerm = getOntologyRepository().getTermByOboID(id);
            List<String> list = new ArrayList<>(2);
            if (patoTerm == null || patoTerm.isSecondary()) {
                list.add(patoString);
                mergedTermList.add(list);
                continue;
            }
            if (patoTerm.isObsolete()) {
                list.add(patoTerm.getOboID());
                list.add(patoTerm.getTermName());
                obsoleteTermList.add(list);
            }
        }

        String reportName = jobName + ".obsoleted-eap-term";
        ReportConfiguration reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, obsoleteTermList, reportConfiguration);

        reportName = jobName + ".merged-eap-term";
        reportConfiguration = new ReportConfiguration(jobName, dataDirectory, reportName, true);
        createErrorReport(null, mergedTermList, reportConfiguration);

        return (mergedTermList.size() > 0 || obsoleteTermList.size() > 0) ? -1 : 0;
    }
}
