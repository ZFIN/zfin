package org.zfin.datatransfer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.Species;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.util.ReportGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 *
 */
public class LoadSignafishJob extends AbstractValidateDataReportTask {

    private static Logger logger = LogManager.getLogger(LoadSignafishJob.class);

    public LoadSignafishJob(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();
        runLoad();

//        DOIProcessor driver = new DOIProcessor(maxAttempts, maxToProcess);

        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.includeTimestamp();
/*
        for (String message : driver.getMessages()) {
            rg.addIntroParagraph(message);
        }
        List<List<String>> updated = driver.getUpdated();
        rg.addDataTable(updated.size() + " Updated Publications", Arrays.asList("Publication", "PubMed ID", "DOI"), updated);
        for (String error : driver.getErrors()) {
            rg.addErrorMessage(error);
        }
        rg.writeFiles(new File(dataDirectory, jobName), jobName);

        return driver.getErrors().size();
*/
        return 0;
    }

    private void runLoad() {
        try {
            HibernateUtil.createTransaction();
            List<String> geneIdList = parseFile();
            load(geneIdList);
            HibernateUtil.flushAndCommitCurrentSession();
            LOG.info("Committed load...");
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
//        createReport();
    }

    private void load(List<String> geneIdList) {

        ReferenceDatabase signafishDb = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.SIGNAFISH,
                ForeignDBDataType.DataType.OTHER,
                ForeignDBDataType.SuperType.SUMMARY_PAGE,
                Species.Type.ZEBRAFISH);

        int numberOfDefeletedIds = getMarkerRepository().deleteMarkerDBLinks(signafishDb, geneIdList);
        getMarkerRepository().addMarkerDBLinks(signafishDb, geneIdList);
        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.includeTimestamp();
        rg.addIntroParagraph("With this load there are now " + getMarkerRepository().getSignafishLinkCount(signafishDb) + " Signafish links in total.");
        rg.addIntroParagraph("Deleted Records: " +numberOfDefeletedIds);
        rg.writeFiles(new File(dataDirectory, jobName), "signafish-report");
    }


    private List<String> parseFile() throws Exception {
        String fileName = "zfin_ids.lst";

        String url = "http://signalink.org/" + fileName;

        URL oracle = new URL(url);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        List<String> idd = new ArrayList<>();
        while ((inputLine = in.readLine()) != null)
            idd.add(inputLine);
        in.close();

        return idd;
    }

    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        String jobName = args[2];
        LoadSignafishJob job = new LoadSignafishJob(jobName, args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
    }
}
