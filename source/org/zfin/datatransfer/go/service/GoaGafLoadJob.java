package org.zfin.datatransfer.go.service;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.zfin.datatransfer.go.*;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.mail.IntegratedJavaMailSender;
import org.zfin.mutant.GafOrganization;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.properties.ZfinProperties;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;

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
public class GoaGafLoadJob implements Job {

    private Logger logger = Logger.getLogger(GoaGafLoadJob.class);

    // these should be autowired, but everything needs to be in the same context, first
//    @Autowired
    private DownloadService downloadService = new DownloadService();
    //    @Autowired
    private GafService gafService = new GafService();
    //    @Autowired
    private GafParser gafParser = new GafParser();

    public GoaGafLoadJob() {
    }

    private final String GOA_DOWNLOAD_URL = "ftp://ftp.geneontology.org/pub/go/gene-associations/submission/gene_association.goa_zebrafish.gz";
    private final String LOCAL_DOWNLOAD_FILE = System.getProperty("java.io.tmpdir") + "/" + "gene_association.goa_zebrafish";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        StringBuilder message = new StringBuilder();

        try {
// 1. download gzipped GAF file
//        ftp://ftp.geneontology.org/pub/go/gene-associations/submission/gene_association.goa_zebrafish.gz
            File downloadedFile = downloadService.downloadGzipFile(new File(LOCAL_DOWNLOAD_FILE)
                    , new URL(GOA_DOWNLOAD_URL)
                    , false);

            // 2. parse file
            List<GafEntry> gafEntries = gafParser.parseGafFile(downloadedFile);

            GafOrganization gafOrganization = RepositoryFactory.getMarkerGoTermEvidenceRepository().getGafOrganization(GafOrganization.OrganizationEnum.GOA);
            // 3. create new GAF entries based on rules
            GafJobData gafJobData = new GafJobData();
            gafJobData.setGafEntryCount(gafEntries.size());

            gafService.processGoaGafEntries(gafEntries, gafJobData);

            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafJobData);
            HibernateUtil.flushAndCommitCurrentSession();

            gafService.generateRemovedEntries(gafJobData, gafOrganization);

            HibernateUtil.createTransaction();
            gafService.removeEntries(gafJobData);
            HibernateUtil.flushAndCommitCurrentSession();

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

            (new IntegratedJavaMailSender()).sendMail("Summary of GOA load: " + (new Date()).toString()
                    , message.toString() + " from file: " + downloadedFile,
                    ZfinProperties.splitValues(ZfinPropertiesEnum.GO_EMAIL_CURATOR));

        } catch (Exception e) {
            logger.error("Failed to process Gaf load job", e);
            HibernateUtil.rollbackTransaction();
            (new IntegratedJavaMailSender()).sendMail("Errors in GOA load: " + (new Date()).toString()
                    , "Error in GOA load: " + e.fillInStackTrace().toString() + "\nNotes:\n" + message.toString(),
                    ZfinProperties.splitValues(ZfinPropertiesEnum.GO_EMAIL_ERR));
        } finally {
            downloadService = null;
            gafService = null;
        }

    }

}
