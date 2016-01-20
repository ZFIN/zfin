package org.zfin.datatransfer.go.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.zfin.datatransfer.go.*;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    private static Logger logger = Logger.getLogger(GafLoadJob.class);

    @Autowired
    protected DownloadService downloadService;

    private final int BATCH_SIZE = 20;

    protected FpInferenceGafParser gafParser;
    protected GafService gafService;
    protected String downloadUrl;
    protected String localDownloadFile;
    protected String organization;

    public int execute() {
        int exitCode = 0;

        GafOrganization.OrganizationEnum organizationEnum = GafOrganization.OrganizationEnum.getType(organization);

        try {
            localDownloadFile = ZfinPropertiesEnum.TARGETROOT + "/server_apps/DB_maintenance/gafLoad/" + "Load-GAF-" + organizationEnum.name() + "-gene_association";
            gafService = new GafService(organizationEnum);

            // 1. download gzipped GAF file
            File downloadedFile = downloadService.downloadFile(new File(localDownloadFile)
                    , new URL(downloadUrl)
                    , false);

            // 2. parse file
            List<GafEntry> gafEntries = gafParser.parseGafFile(downloadedFile);

            if (CollectionUtils.isEmpty(gafEntries)) {
                throw new GafValidationError("No gaf entries found in file: " + downloadedFile);
            }

            // 2.5 replace merged ZDB Id
            // added this step for FB case 7957 "GAF load should handle merged markers"
            gafService.replaceMergedZDBIds(gafEntries);

            GafOrganization gafOrganization = RepositoryFactory.getMarkerGoTermEvidenceRepository()
                    .getGafOrganization(organizationEnum);
            // 3. create new GAF entries based on rules
            GafJobData gafJobData = new GafJobData();
            gafJobData.setGafEntryCount(gafEntries.size());

            gafService.processEntries(gafEntries, gafJobData);

            addAnnotations(gafJobData);
            updateAnnotations(gafJobData);

            gafService.generateRemovedEntries(gafJobData, gafOrganization);

            removeAnnotations(gafJobData);

            FileWriter summary = new FileWriter(new File(dataDirectory, jobName + "_summary.txt"));
            FileWriter details = new FileWriter(new File(dataDirectory, jobName + "_details.txt"));

            summary.append(gafJobData.toString());
            summary.flush();
            summary.close();

            details.write("== REMOVED ==\n");
            for (GafJobEntry removed : gafJobData.getRemovedEntries()) {
                details.append(removed.getEntryString()).append("\n");
            }
            details.append("\n\n");

            details.append("== ADDED ==").append("\n");
            for (MarkerGoTermEvidence entry : gafJobData.getNewEntries()) {
                details.append(entry.toString()).append("\n").append("\n");
            }
            details.append("\n\n");

            details.append("== UPDATED ==").append("\n");
            for (MarkerGoTermEvidence entry : gafJobData.getUpdateEntries()) {
                details.append(entry.toString()).append("\n").append("\n");
            }
            details.append("\n\n");

            details.append("== ERRORS ==").append("\n");
            for (GafValidationError error : gafJobData.getErrors()) {
                details.append(error.getMessage()).append("\n");
            }
            details.append("\n\n");

            details.append("== EXISTING ==").append("\n");
            for (GafJobEntry entry : gafJobData.getExistingEntries()) {
                details.append(entry.toString()).append("\n").append("\n");
            }
            details.append("\n\n");

            details.flush();
            details.close();

        } catch (Exception e) {
            logger.error("Failed to process Gaf load job", e);
            if (HibernateUtil.currentSession().getTransaction() != null) {
                HibernateUtil.rollbackTransaction();
            }
            try {
                FileWriter errors = new FileWriter(new File(dataDirectory, jobName + "_errors.txt"));
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
        return exitCode;
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
                gafService.addAnnotationsBatch(batchToAdd, gafJobData);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                String error = "Failed to add batch: ";
                for (MarkerGoTermEvidence markerGoTermEvidence : batchToAdd) {
                    error += markerGoTermEvidence.toString() + "\n";
                }
                GafValidationError gafValidationError = new GafValidationError(error, e);
                logger.error(gafValidationError);
                gafJobData.addError(gafValidationError);
            } finally {
                // the theory is that these were being left open . . . this should fix that
                HibernateUtil.closeSession();
            }
        }
    }

    private void updateAnnotations(GafJobData gafJobData) {
        Set<MarkerGoTermEvidence> evidencesToUpdate = gafJobData.getUpdateEntries();
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
        while (iteratorToRemove.hasNext()) {
            // build batch
            List<GafJobEntry> batchToRemove = new ArrayList<>();
            for (int i = 0; i < BATCH_SIZE && iteratorToRemove.hasNext(); ++i) {
                GafJobEntry removeEntry = iteratorToRemove.next();
                batchToRemove.add(removeEntry);
            }
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
        String parserClassName = args[5];
        try {
            job.gafParser = (FpInferenceGafParser) context.getBean(Class.forName(parserClassName));
        } catch (ClassNotFoundException e) {
            logger.error("Could not load parser class " + parserClassName);
            System.exit(1);
        }
        job.initDatabase();
        System.exit(job.execute());
    }
}
