package org.zfin.datatransfer.go.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.zfin.datatransfer.go.*;
import org.zfin.datatransfer.persistence.LoadFileLog;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.util.ZfinSystemUtils.envTrue;

/**
 * This is autowired for spring 3, but is not in the correct context yet.
 * <p/>
 * Created for Case 6447 etc.
 * <p/>
 * References:
 * http://fogbugz.zfin.org/default.asp?6447
 * http://fogbugz.zfin.org/default.asp?W739
 * https://wiki.zfin.org/display/doc/Gene+Association+File+-+GOA+Load
 * <p/>
 * <p/>
 * Loads Gene Association Files (GAF) from the Gene Ontology Annotation (GOA) at Uniprot:
 * <p/>
 * http://www.ebi.ac.uk/GOA/RGI/index.html
 * <p/>
 * <p/>
 * General flow of job in GoaGafLoadJob:
 * - download gaf file if newer (DownloadService)
 * - parse gaf file, excluding obvious entries (not reported) (GafParser)
 * - use uniprot ID to get dblink and get associated gene (or genes if marker hits clone).  (GafService)
 * - generate annotation (GafService) and see if it matches an existing annotation (MarkerGoTermEvidence) or is
 * less specific than an existing one (mark as "existing") or determine if a valid annotation ("report as error")
 * or should be scheduled to be added ("add new entry").
 * - Following processing, call gafService.addAnnotations which adds the annotations all at once.
 * In one edge-case, they will mark an annotation to be removed, but generally its just a batch add.
 * - Following the add, we determine the entries to be removed by seeing if any of the existing entries are
 * not in the new set.  These are added to the report as "removed".
 * - GafService.removeEntries() is called to do a batch removal.
 * - Messages are created and emailed out.
 */
@Component
public class GafLoadJob extends AbstractValidateDataReportTask {

    private static Logger logger = LogManager.getLogger(GafLoadJob.class);

    @Autowired
    protected DownloadService downloadService;

    private final int BATCH_SIZE = 100;

    protected FpInferenceGafParser gafParser;
    protected GafService gafService;
    protected String downloadUrl;
    protected String downloadUrl2;
    protected String downloadUrl3;
    protected String localDownloadFile;
    protected String localDownloadFile2;
    protected String localDownloadFile3;
    protected File downloadedFile;
    protected File downloadedFile2;
    protected File downloadedFile3;
    protected String organization;
    protected Boolean skipDownloadIfUnchanged; //default to false

    public int execute() {
        int exitCode = 0;
        setLoggerFile();

        clearReportDirectory();

        GafOrganization.OrganizationEnum organizationEnum = GafOrganization.OrganizationEnum.getType(organization);

        Date lastModified = null;
        try {
            lastModified = downloadService.getLastModifiedOnServer(new URL(downloadUrl));
            boolean alreadyProcessed = downloadService.isDownloadAlreadyProcessed(downloadUrl, organizationEnum, lastModified);
            if (skipDownloadIfUnchanged != null && skipDownloadIfUnchanged && alreadyProcessed) {
                logger.info("Download for " + new SimpleDateFormat("yyyy-MM-dd").format(lastModified)
                        + " has already been processed and skipDownloadIfUnchanged is true.  " +
                        "Exiting load for " + organizationEnum.name());
                return exitCode;
            }

            localDownloadFile = ZfinPropertiesEnum.TARGETROOT + "/server_apps/DB_maintenance/gafLoad/" + jobName + "/" + "Load-GAF-" + organizationEnum.name() + "-gene_association";

            gafService = new GafService(organizationEnum);
            // File downloadedFile = downloadService.downloadFile(new File(localDownloadFile)
            // 1. download gzipped GAF file
            downloadedFile = downloadService.downloadFile(new File(localDownloadFile)
                , new URL(downloadUrl)
                , false);

            if (organization.equals("GOA")) {
                localDownloadFile2 = ZfinPropertiesEnum.TARGETROOT + "/server_apps/DB_maintenance/gafLoad/" + jobName + "/" + "Load-GAF-" + organizationEnum.name() + "-gene_association2";
                downloadedFile2 = downloadService.downloadFile(new File(localDownloadFile2)
                    , new URL(downloadUrl2)
                    , false);
                String downloadFile2Str = FileUtils.readFileToString(downloadedFile2);
                FileUtils.write(downloadedFile, downloadFile2Str, true); // true for append
                localDownloadFile3 = ZfinPropertiesEnum.TARGETROOT + "/server_apps/DB_maintenance/gafLoad/" + jobName + "/" + "Load-GAF-" + organizationEnum.name() + "-gene_association3";
                downloadedFile3 = downloadService.downloadFile(new File(localDownloadFile3)
                    , new URL(downloadUrl3)
                    , false);
                String downloadFile3Str = FileUtils.readFileToString(downloadedFile3);
                FileUtils.write(downloadedFile, downloadFile3Str, true); // true for append
            }

            // 2. parse file
            List<GafEntry> gafEntries = gafParser.parseGafFile(downloadedFile);
            gafParser.postProcessing(gafEntries);
            System.out.print("Gaf Entries: ");
            System.out.printf("%,d%n", gafEntries.size());
            int sizeentry = gafEntries.size();
//            System.out.println(gafEntries.get(sizeentry - 1).getCol8pipes());

            if (CollectionUtils.isEmpty(gafEntries)) {
                throw new GafValidationError("No gaf entries found in file: " + downloadedFile);
            }

            // 2.5 replace merged ZDB Id
            // added this step for FB case 7957 "GAF load should handle merged markers"
            GafJobData gafJobData = new GafJobData();
            gafJobData.setInfPipeCount(gafEntries.get(sizeentry - 1).getCol8pipes());
            gafJobData.setInfCommaCount(gafEntries.get(sizeentry - 1).getCol8commas());
            gafJobData.setInfBothCount(gafEntries.get(sizeentry - 1).getCol8both());

            gafService.replaceMergedZDBIds(gafEntries);

            GafOrganization gafOrganization = RepositoryFactory.getMarkerGoTermEvidenceRepository()
                .getGafOrganization(organizationEnum);
            // 3. create new GAF entries based on rules

            gafJobData.setGafEntryCount(gafEntries.size());

            logger.info("Before Processing");
            // strip off global prefix, ZFIN, on gene IDs
            gafEntries.forEach(gafEntry -> {
                String entryId = gafEntry.getEntryId();
                gafEntry.setEntryId(entryId.substring(entryId.indexOf(":") + 1));
            });
            gafService.processEntries(gafEntries, gafJobData);
            gafService.generateRemovedEntries(gafJobData, gafOrganization);
            List<GafJobEntry> optional = Optional.ofNullable(gafJobData.getRemovedEntries()).orElse(new ArrayList<>());
            System.out.println("Removed entries: " + optional.size());
            logger.info("Before adding new one");
            addAnnotations(gafJobData);
            logger.info("Before updating");
            updateAnnotations(gafJobData);

            logger.info("Before removing");
            removeAnnotations(gafJobData);
            FileWriter summary = new FileWriter(new File(new File(dataDirectory, jobName), jobName + "_summary.txt"));
            FileWriter details = new FileWriter(new File(new File(dataDirectory, jobName), jobName + "_details.txt"));

            summary.append(gafJobData.toString());
            summary.flush();
            summary.close();

            details.append("\n\n");
            details.write("== REMOVED == " + optional.size());
            for (GafJobEntry removed : gafJobData.getRemovedEntries()) {
                details.append(removed.getEntryString()).append("\n");
            }
            details.append("\n\n");

            details.append("== ADDED == " + gafJobData.getNewEntries().size()).append("\n");
            for (MarkerGoTermEvidence entry : gafJobData.getNewEntries()) {
                details.append(entry.toString()).append("\n").append("\n");
            }
            details.append("\n\n");

            details.append("== UPDATED == " + gafJobData.getUpdateEntries().size()).append("\n");
            for (MarkerGoTermEvidence entry : gafJobData.getUpdateEntries()) {
                details.append(entry.toString()).append("\n").append("\n");
            }
            details.append("\n\n");

            details.append("== ERRORS == " + gafJobData.getErrors().size()).append("\n");
            for (GafValidationError error : gafJobData.getErrors()) {
                details.append(error.getMessage()).append("\n");
            }
            details.append("\n\n");

            details.append("== EXISTING ==" + gafJobData.getExistingEntries().size()).append("\n");
            for (GafJobEntry entry : gafJobData.getExistingEntries()) {
                details.append(entry.toString()).append("\n").append("\n");
            }
            details.append("\n\n");

            details.flush();
            details.close();

            //throw an exception if parser encountered an error
            //do this at the end so the load works for records that are valid
            if (gafParser.isErrorEncountered()) {
                System.out.println(gafParser.getErrorMessage());
                System.err.println(gafParser.getErrorMessage());
                exitCode = 2;
            }

        } catch (Exception e) {
            logger.error("Failed to process Gaf load job", e);
            if (HibernateUtil.currentSession().getTransaction() != null) {
                HibernateUtil.rollbackTransaction();
            }
            try {
                FileWriter errors = new FileWriter(new File(new File(dataDirectory, jobName), jobName + "_errors.txt"));
                errors.append("Error in ").append(organizationEnum.toString()).append(" load.\n");
                errors.append(ExceptionUtils.getStackTrace(e));
                errors.flush();
                errors.close();
            } catch (IOException io) {
                logger.error("Error writing error report", io);
            }
            exitCode = 1;
        } finally {
            downloadService = null;
            gafService = null;
            HibernateUtil.closeSession();
        }
        if (exitCode == 0) {
            logValidationReport(organizationEnum.toString(), "GAF Load Job completed successfully.", lastModified);
        }
        return exitCode;
    }

    private void logValidationReport(String loadName, String notes, Date lastModified) {
        HibernateUtil.createTransaction();

        LoadFileLog loadFileLog = new LoadFileLog();
        loadFileLog.setLoadName(loadName);
        loadFileLog.setFilename(downloadedFile.getName());
        loadFileLog.setSource(downloadUrl);
        loadFileLog.setSize(downloadedFile.length());
        loadFileLog.setPath(downloadedFile.getAbsolutePath());
        loadFileLog.setNotes(notes);
        loadFileLog.setDate(lastModified);
        loadFileLog.setProcessedDate(new Date());
        loadFileLog.setReleaseNumber(new SimpleDateFormat("yyyy-MM-dd").format(lastModified));
        try {
            loadFileLog.setMd5(DigestUtils.md5Hex(FileUtils.openInputStream(downloadedFile)));
        } catch (IOException e) {
            logger.error("Could not calculate md5 for file: " + downloadedFile.getAbsolutePath(), e);
        }

        HibernateUtil.currentSession().save(loadFileLog);

        if (organization.equals("GOA")) {
            //log second and third parts of GOA load as well
            LoadFileLog loadFileLog2 = new LoadFileLog();
            loadFileLog2.setLoadName(loadName);
            loadFileLog2.setFilename(downloadedFile2.getName());
            loadFileLog2.setSource(downloadUrl2);
            loadFileLog2.setSize(downloadedFile2.length());
            loadFileLog2.setPath(downloadedFile2.getAbsolutePath());
            loadFileLog2.setNotes("Part 2 of GOA load. " + notes);
            loadFileLog2.setDate(lastModified);
            loadFileLog2.setProcessedDate(new Date());
            loadFileLog2.setReleaseNumber(new SimpleDateFormat("yyyy-MM-dd").format(lastModified));
            try {
                loadFileLog2.setMd5(DigestUtils.md5Hex(FileUtils.openInputStream(downloadedFile2)));
            } catch (IOException e) {
                logger.error("Could not calculate md5 for file: " + downloadedFile2.getAbsolutePath(), e);
            }
            HibernateUtil.currentSession().save(loadFileLog2);

            LoadFileLog loadFileLog3 = new LoadFileLog();
            loadFileLog3.setLoadName(loadName);
            loadFileLog3.setFilename(downloadedFile3.getName());
            loadFileLog3.setSource(downloadUrl3);
            loadFileLog3.setSize(downloadedFile3.length());
            loadFileLog3.setPath(downloadedFile3.getAbsolutePath());
            loadFileLog3.setNotes("Part 3 of GOA load. " + notes);
            loadFileLog3.setDate(lastModified);
            loadFileLog3.setProcessedDate(new Date());
            loadFileLog3.setReleaseNumber(new SimpleDateFormat("yyyy-MM-dd").format(lastModified));
            try {
                loadFileLog3.setMd5(DigestUtils.md5Hex(FileUtils.openInputStream(downloadedFile3)));
            } catch (IOException e) {
                logger.error("Could not calculate md5 for file: " + downloadedFile3.getAbsolutePath(), e);
            }
            HibernateUtil.currentSession().save(loadFileLog3);
        }

        HibernateUtil.flushAndCommitCurrentSession();
    }

    private void addAnnotations(GafJobData gafJobData) {
        Set<MarkerGoTermEvidence> evidencesToAdd = gafJobData.getNewEntries();
        Iterator<MarkerGoTermEvidence> iteratorToAdd = evidencesToAdd.iterator();

        while (iteratorToAdd.hasNext()) {
            // build batch
            List<MarkerGoTermEvidence> batchToAdd = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE && iteratorToAdd.hasNext(); ++i) {
                MarkerGoTermEvidence markerGoTermEvidence = iteratorToAdd.next();
                batchToAdd.add(markerGoTermEvidence);
            }
            try {
                HibernateUtil.createTransaction();
                gafService.addAnnotationsBatch(batchToAdd, gafJobData, gafParser instanceof GpadParser);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                String error = "Failed to add batch: ";
                for (MarkerGoTermEvidence markerGoTermEvidence : batchToAdd) {
                    error += markerGoTermEvidence.toString() + "\n";
                }
                GafValidationError gafValidationError = new GafValidationError(error, e);
                logger.error(error, e);
                gafJobData.addError(gafValidationError);
            } finally {
                // the theory is that these were being left open . . . this should fix that
                HibernateUtil.closeSession();
            }
        }
    }

    private void updateAnnotations(GafJobData gafJobData) {
        Set<MarkerGoTermEvidence> evidencesToUpdate = gafJobData.getUpdateEntries();
        System.out.println("updated Annotations: " + gafJobData.getUpdateEntries().size());
        Iterator<MarkerGoTermEvidence> iteratorToUpdate = evidencesToUpdate.iterator();

        while (iteratorToUpdate.hasNext()) {
            // build batch
            List<MarkerGoTermEvidence> batchToUpdate = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE && iteratorToUpdate.hasNext(); ++i) {
                batchToUpdate.add(iteratorToUpdate.next());
            }
            try {
                HibernateUtil.createTransaction();
                gafService.updateEntriesBatch(batchToUpdate);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                String error = "Failed to update batch: ";
                for (MarkerGoTermEvidence markerGoTermEvidence : batchToUpdate) {
                    error += markerGoTermEvidence.toString() + "\n";
                }
                GafValidationError gafValidationError = new GafValidationError(error, e);
                logger.error(gafValidationError);
                gafJobData.addError(gafValidationError);
            } finally {
                HibernateUtil.closeSession();
            }
        }
    }

    private void removeAnnotations(GafJobData gafJobData) {
        List<GafJobEntry> evidencesToRemove = gafJobData.getRemovedEntries();

        Iterator<GafJobEntry> iteratorToRemove = evidencesToRemove.iterator();

        // create batch
        int index = 0;
        while (iteratorToRemove.hasNext()) {
            // build batch
            List<GafJobEntry> batchToRemove = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE && iteratorToRemove.hasNext(); ++i) {
                GafJobEntry removeEntry = iteratorToRemove.next();
                batchToRemove.add(removeEntry);
            }
            System.out.print(index++ + ":");
            try {
                HibernateUtil.createTransaction();
                gafService.removeEntriesBatch(batchToRemove, gafJobData);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                String error = "Failed to remove batch: ";
                for (GafJobEntry evidenceToRemove : batchToRemove) {
                    error += evidenceToRemove.toString() + "\n";
                }
                GafValidationError gafValidationError = new GafValidationError(error, e);
                logger.error(gafValidationError);
                gafJobData.addError(gafValidationError);
            }
        }
    }

    public static void main11(String[] args) throws InterruptedException {
        // create automatic thread pool (by default: size of number of cores)
        ExecutorService executor = Executors.newWorkStealingPool();

// define a list of 3 callables each of which return a String
        List<Callable<String>> callables = Arrays.asList(
            () -> "task1",
            () -> "task2",
            () -> "task3");

// submit them all and handle each callable's Future object
        executor.invokeAll(callables)
            .stream()
            .map(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            })
            .forEach(System.out::println);
    }

    /**
     * @param args Expecting the following positional arguments:
     *             propertyFilePath
     *             baseDir
     *             jobName
     *             organization
     *             downloadUrl
     *             parserClassName
     *             downloadUrl2
     *             downloadUrl3
     */
    public static void main(String[] args) {
        initLog4J();
        setLoggerToInfoLevel(logger);
        String baseDir = args[1];
        String configPath = "file:" + baseDir + "/gaf-load.xml";
        ApplicationContext context = new FileSystemXmlApplicationContext(configPath);
        GafLoadJob job = context.getBean(GafLoadJob.class);
        job.initBasicInfo(args[2], args[0], baseDir);
        job.organization = args[3];
        job.downloadUrl = args[4];
        if (job.organization.equals("GOA")) {
            job.downloadUrl2 = args[6];
            job.downloadUrl3 = args[7];
        }
        String parserClassName = args[5];

        job.skipDownloadIfUnchanged = envTrue("SKIP_DOWNLOAD_IF_UNCHANGED");

        try {
            job.gafParser = (FpInferenceGafParser) context.getBean(Class.forName(parserClassName));

            //if load-noctua-gpad job, then make sure to validate all ZDB IDs in inferences.
            if (job.gafParser instanceof GpadParser) {
                ((GpadParser) job.gafParser).setValidateInferences(true);
            }
        } catch (ClassNotFoundException e) {
            logger.error("Could not load parser class " + parserClassName);
            System.exit(1);
        }
        job.initDatabase();
        int exitCode = job.execute();
        if (exitCode == 0) {
            logger.info("GAF Load Job completed successfully.");
        } else {
            logger.error("GAF Load Job completed with errors.  Exit code: " + exitCode);
        }
        System.exit(exitCode);
    }
}
