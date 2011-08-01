package org.zfin.datatransfer.go.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.datatransfer.go.*;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * This is autowired for spring 3, but is not in the correct context yet.
 * <p/>
 * Created for Case 6447.
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
//@Component
public class GafLoadJob implements Job {

    private Logger logger = Logger.getLogger(GafLoadJob.class);

    // these should be autowired, but everything needs to be in the same context, first
//    @Autowired
    protected DownloadService downloadService = new DownloadService();
    //    @Autowired
    protected GafService gafService ;
    //    @Autowired
    protected FpInferenceGafParser gafParser ;
    private final int BATCH_SIZE = 20;

    protected String downloadUrl ;
    protected String localDownloadFile ;
    protected GafOrganization.OrganizationEnum organizationEnum ;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {


        StringBuilder message = new StringBuilder();
        try {
            setFields(jobExecutionContext.getJobDetail().getJobDataMap());
// 1. download gzipped GAF file
//        ftp://ftp.geneontology.org/pub/go/gene-associations/submission/gene_association.goa_zebrafish.gz
            File downloadedFile = downloadService.downloadFile(new File(localDownloadFile)
                    , new URL(downloadUrl)
                    , false);

            // 2. parse file
            List<GafEntry> gafEntries = gafParser.parseGafFile(downloadedFile);

            if(CollectionUtils.isEmpty(gafEntries)){
                throw new GafValidationError("No gaf entries found in file: "+downloadedFile);
            }

            GafOrganization gafOrganization = RepositoryFactory.getMarkerGoTermEvidenceRepository()
                    .getGafOrganization(organizationEnum);
            // 3. create new GAF entries based on rules
            GafJobData gafJobData = new GafJobData();
            gafJobData.setGafEntryCount(gafEntries.size());

            gafService.processEntries(gafEntries, gafJobData);

            addAnnotations(gafJobData);
//            HibernateUtil.createTransaction();
//            gafService.addAnnotations(gafJobData);
//            HibernateUtil.flushAndCommitCurrentSession();

            gafService.generateRemovedEntries(gafJobData, gafOrganization);

            removeAnnotations(gafJobData);
//            HibernateUtil.createTransaction();
//            gafService.removeEntries(gafJobData);
//            HibernateUtil.flushAndCommitCurrentSession();

            String summary = gafJobData.toString();
            message.append(summary).append("\n\n");

            message.append("REMOVED").append("\n");
            for (GafJobEntry removed : gafJobData.getRemovedEntries()) {
                message.append(removed.getEntryString()).append("\n");
            }
            message.append("\n\n");

            message.append("ADDED").append("\n");
            for (MarkerGoTermEvidence entry : gafJobData.getNewEntries()) {
                message.append(entry.toString()).append("\n").append("\n");
            }
            message.append("\n\n");

            message.append("ERRORS").append("\n");
            for (GafValidationError error : gafJobData.getErrors()) {
                message.append(error.getMessage()).append("\n");
            }
            message.append("\n\n");

            message.append("EXISTING").append("\n");
            for (GafJobEntry entry : gafJobData.getExistingEntries()) {
                message.append(entry.toString()).append("\n").append("\n");
            }
            message.append("\n\n");

            (new IntegratedJavaMailSender()).sendMail("Summary of "+organizationEnum + " load: " + (new Date()).toString()
                    , message.toString() + " from file: " + downloadedFile,
                    ZfinProperties.splitValues(ZfinPropertiesEnum.GO_EMAIL_CURATOR));

        } catch (Exception e) {
            logger.error("Failed to process Gaf load job", e);
            if(HibernateUtil.currentSession().getTransaction()!=null){
                HibernateUtil.rollbackTransaction();
            }
            (new IntegratedJavaMailSender()).sendMail("Errors in " + organizationEnum + " load: " + (new Date()).toString()
                    , "Error in " + organizationEnum + " load: " + e.fillInStackTrace().toString() + "\nNotes:\n" + message.toString(),
                    ZfinProperties.splitValues(ZfinPropertiesEnum.GO_EMAIL_ERR));
        } finally {
            downloadService = null;
            gafService = null;
            HibernateUtil.closeSession();
        }

    }

    private void setFields(JobDataMap jobDataMap) {
        downloadService = (DownloadService) jobDataMap.get("downloadService");
        String enumString = jobDataMap.get("organization").toString();

        // this sets the localDownloadFile, as well and gafService
        organizationEnum = GafOrganization.OrganizationEnum.getType(enumString);
        localDownloadFile = System.getProperty("java.io.tmpdir") + "/" + "gene_association."+organizationEnum.name();
        gafService = new GafService(organizationEnum);

        downloadUrl = jobDataMap.get("downloadUrl").toString();
        gafParser = (FpInferenceGafParser) jobDataMap.get("gafParser");
    }

    private void addAnnotations(GafJobData gafJobData) {
        Set<MarkerGoTermEvidence> evidencesToAdd = gafJobData.getNewEntries();
        Iterator<MarkerGoTermEvidence> iteratorToAdd = evidencesToAdd.iterator();

        while(iteratorToAdd.hasNext()){
            // build batch
            List<MarkerGoTermEvidence> batchToAdd = new ArrayList<MarkerGoTermEvidence>();
            for(int i = 0 ; i < BATCH_SIZE && iteratorToAdd.hasNext() ; ++i){
                MarkerGoTermEvidence markerGoTermEvidence = iteratorToAdd.next();
                batchToAdd.add(markerGoTermEvidence);
            }
            try {
                HibernateUtil.createTransaction();
                gafService.addAnnotationsBatch(batchToAdd,gafJobData);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                String error = "Failed to add batch: " ;
                for(MarkerGoTermEvidence markerGoTermEvidence : batchToAdd ){
                    error += markerGoTermEvidence.toString() + "\n";
                }
                GafValidationError gafValidationError = new GafValidationError(error,e) ;
                logger.error(gafValidationError);
                gafJobData.addError(gafValidationError);
            }
            // the theory is that these were being left open . . . this should fix that
            finally{
                HibernateUtil.closeSession();
            }
        }
    }

    private void removeAnnotations(GafJobData gafJobData) {
        List<GafJobEntry> evidencesToRemove = gafJobData.getRemovedEntries();
        Iterator<GafJobEntry> iteratorToRemove  = evidencesToRemove.iterator();

        // create batch
        while(iteratorToRemove.hasNext()){
            // build batch
            List<GafJobEntry> batchToRemove = new ArrayList<GafJobEntry>();
            for(int i = 0 ; i < BATCH_SIZE && iteratorToRemove.hasNext() ; ++i){
                GafJobEntry removeEntry = iteratorToRemove.next();
                batchToRemove.add(removeEntry);
            }
            try {
                HibernateUtil.createTransaction();
                gafService.removeEntriesBatch(batchToRemove, gafJobData);
                HibernateUtil.flushAndCommitCurrentSession();
            } catch (Exception e) {
                HibernateUtil.rollbackTransaction();
                String error = "Failed to remove batch: " ;
                for(GafJobEntry evidenceToRemove : batchToRemove ){
                    error += evidenceToRemove.toString() + "\n";
                }
                GafValidationError gafValidationError = new GafValidationError(error,e) ;
                logger.error(gafValidationError);
                gafJobData.addError(gafValidationError);
            }
        }
    }

}
