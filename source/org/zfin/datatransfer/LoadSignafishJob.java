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

    // Must be https: the http URL 302-redirects to https, and URL.openStream() does not follow
    // cross-protocol redirects, so http yields an empty body (0 IDs). See Load-Signafish_w build #516.
    static final String DEFAULT_SOURCE_URL = "https://signalink.org/zfin_ids.lst";

    // Environment variable to override the upstream source URL (e.g. for testing against a fixture).
    static final String SOURCE_URL_ENV_VAR = "SIGNAFISH_SOURCE_URL";

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

        if (signafishDb == null) {
            throw new RuntimeException("Could not find the Signafish reference database "
                    + "(" + ForeignDB.AvailableName.SIGNAFISH + "/" + ForeignDBDataType.DataType.OTHER
                    + "/" + ForeignDBDataType.SuperType.SUMMARY_PAGE + "/" + Species.Type.ZEBRAFISH + "). "
                    + "Aborting before any links are modified.");
        }

        long existingLinkCount = getMarkerRepository().getSignafishLinkCount(signafishDb);
        logger.info("Signafish reference database: " + signafishDb.getZdbID());
        logger.info("Existing Signafish links before load: " + existingLinkCount);
        logger.info("Gene IDs parsed from source: " + geneIdList.size());

        // Guard against a destructive load. The load deletes every existing link not present in the
        // incoming list, so an empty (or anomalously small) input file would silently wipe out all
        // links. Abort before touching the database so the transaction rolls back and the build fails
        // loudly instead of reporting "0 links" as a success. See ZFIN Load-Signafish_w build #516.
        if (geneIdList.isEmpty()) {
            throw new RuntimeException("Parsed an empty Signafish gene ID list from the source file. "
                    + "Refusing to delete the " + existingLinkCount + " existing Signafish link(s). "
                    + "Check that the source file is reachable and populated.");
        }

        int numberOfDeletedIds = getMarkerRepository().deleteMarkerDBLinksNotInList(signafishDb, geneIdList);
        getMarkerRepository().addMarkerDBLinks(signafishDb, geneIdList);
        long finalLinkCount = getMarkerRepository().getSignafishLinkCount(signafishDb);
        logger.info("Deleted " + numberOfDeletedIds + " Signafish link(s) not in the incoming list");
        logger.info("Signafish links after load: " + finalLinkCount + " (was " + existingLinkCount + ")");

        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.includeTimestamp();
        rg.addIntroParagraph("Gene IDs parsed from source: " + geneIdList.size());
        rg.addIntroParagraph("With this load there are now " + finalLinkCount + " Signafish links in total (was " + existingLinkCount + ").");
        rg.addIntroParagraph("Deleted Records: " + numberOfDeletedIds);
        rg.writeFiles(new File(dataDirectory, jobName), "signafish-report");
    }


    private String getSourceUrl() {
        String override = System.getenv(SOURCE_URL_ENV_VAR);
        if (override != null && !override.trim().isEmpty()) {
            logger.info("Overriding Signafish source URL from $" + SOURCE_URL_ENV_VAR);
            return override.trim();
        }
        return DEFAULT_SOURCE_URL;
    }

    private List<String> parseFile() throws Exception {
        String url = getSourceUrl();
        logger.info("Fetching Signafish gene ID list from " + url);

        URL oracle = new URL(url);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        int totalLines = 0;
        List<String> idd = new ArrayList<>();
        while ((inputLine = in.readLine()) != null) {
            totalLines++;
            String trimmed = inputLine.trim();
            if (!trimmed.isEmpty()) {
                idd.add(trimmed);
            }
        }
        in.close();

        logger.info("Read " + totalLines + " line(s) from " + url + ", " + idd.size() + " non-blank gene ID(s)");
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
