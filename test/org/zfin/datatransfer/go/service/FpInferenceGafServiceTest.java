package org.zfin.datatransfer.go.service;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.FpInferenceGafParser;
import org.zfin.datatransfer.go.GafEntry;
import org.zfin.datatransfer.go.GafJobData;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.mutant.MarkerGoTermEvidence;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests GafService methods
 */
public class FpInferenceGafServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(FpInferenceGafServiceTest.class);
    private GafService gafService = new GafService(GafOrganization.OrganizationEnum.FP_INFERENCES);
    private FpInferenceGafParser gafParser = new FpInferenceGafParser();

    // this is a pub with no go evidence annotations and is closed, so none will be added
    private final String DEFAULT_TEST_ACCESSION = "PMID:10630700"; // "ZDB-PUB-000118-16"

    private final String FP_INFERENCE_DIRECTORY = "test/gaf/fp_inference/" ;


    /**
     * All annotations should have this pub so subsequent loads to break the code.
     *
     * @param gafEntries
     */
    private void makeTestPub(List<GafEntry> gafEntries) {
        for (GafEntry gafEntry : gafEntries) {
            gafEntry.setPubmedId(DEFAULT_TEST_ACCESSION);
        }
    }

    @Test
    public void testEMBL() throws Exception {
        File file = new File(FP_INFERENCE_DIRECTORY +"gene_association.zfin_test2");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(1, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafJobData = new GafJobData();

        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("existing: " + gafJobData.getExistingEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());


        assertEquals(0, gafJobData.getErrors().size());
        assertEquals(1, gafJobData.getNewEntries().size() + gafJobData.getExistingEntries().size());
        assertEquals(0, gafJobData.getRemovedEntries().size());

    }


    @Test
    public void fpInferenceBadInference1() throws Exception {
        File file = new File(FP_INFERENCE_DIRECTORY +"gene_association.zfin_test3");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(1, gafEntries.size());
//        makeTestPub(gafEntries);

        GafJobData gafJobData = new GafJobData();

        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("existing: " + gafJobData.getExistingEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());


        assertEquals(0, gafJobData.getErrors().size());
        // this will end up being
        assertEquals(1, gafJobData.getNewEntries().size() + gafJobData.getExistingEntries().size());
        assertEquals(0, gafJobData.getRemovedEntries().size());

    }


    /**
     * Just tests a normal load
     * @throws Exception
     */
    @Test
    public void fpInferenceTest1() throws Exception {
        File file = new File(FP_INFERENCE_DIRECTORY + "gene_association.zfin_test1");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(23, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafJobData = new GafJobData();

        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("existing: " + gafJobData.getExistingEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());


        assertEquals(0, gafJobData.getErrors().size());
        assertEquals(0, gafJobData.getExistingEntries().size());
        assertEquals(23, gafJobData.getNewEntries().size());
        assertEquals(0, gafJobData.getRemovedEntries().size());

        int count = 0 ;
        for(MarkerGoTermEvidence markerGoTermEvidence : gafJobData.getNewEntries()){
            if(count==5){
                assertEquals("UniProtKB:P01344",markerGoTermEvidence.getInferredFrom().iterator().next().getInferredFrom());
            }
            if(count==13){
                assertEquals("ZFIN:ZDB-GENE-980526-290",markerGoTermEvidence.getInferredFrom().iterator().next().getInferredFrom());
            }
            ++count ;
        }
    }

    // just makes sure that the service is still there, not something we want to run all of the time.
//    @Test
    public void testDownloadFpInference() throws Exception {
        DownloadService downloadService = new DownloadService();
//        File downloadedFile = downloadService.downloadFileFtp(new File(System.getProperty("java.io.tmpdir") + "/" + "gene_association.goa_zebrafish")
//                , new URL("ftp://ftp.geneontology.org/pub/go/scratch/gaf-inference/gene_association.zfin.inf.gaf")
//                , true
//                , false);
        File downloadedFile = downloadService.downloadFile(new File(System.getProperty("java.io.tmpdir") + "/" + "gene_association.goa_zebrafish")
                , new URL("ftp://ftp.geneontology.org/pub/go/scratch/gaf-inference/gene_association.zfin.inf.gaf")
                , true
        );
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.delete());
    }

}
