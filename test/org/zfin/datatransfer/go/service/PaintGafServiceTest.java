package org.zfin.datatransfer.go.service;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.*;
import org.zfin.datatransfer.service.DownloadService;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests GafService methods
 */
public class PaintGafServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(PaintGafServiceTest.class);
    private GafService gafService = new GafService(GafOrganization.OrganizationEnum.PAINT);
    private FpInferenceGafParser gafParser = new PaintGafParser();

    // this is a pub with no go evidence annotations and is closed, so none will be added
    private final String DEFAULT_TEST_ACCESSION = "PMID:10630700"; // "ZDB-PUB-000118-16"

    private final String PAINT_DIRECTORY = "test/gaf/paint/" ;


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
    public void simpleTest() throws Exception{
        File file = new File(PAINT_DIRECTORY+"gene_association.paint_zfin_test1");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(19, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafJobData = new GafJobData();

        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("existing: " + gafJobData.getExistingEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());


        assertEquals(0, gafJobData.getErrors().size());
        assertEquals(19, gafJobData.getNewEntries().size() + gafJobData.getExistingEntries().size());
        assertEquals(0, gafJobData.getRemovedEntries().size());
    }

    @Test
    public void badGeneTest() throws Exception{
        File file = new File(PAINT_DIRECTORY+"gene_association.paint_zfin_test2");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(1, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafJobData = new GafJobData();

        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("existing: " + gafJobData.getExistingEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());


        // a bad gene should post an error
        assertEquals(1, gafJobData.getErrors().size());
        assertEquals(0, gafJobData.getNewEntries().size());
        assertEquals(0, gafJobData.getRemovedEntries().size());
    }


//        @Test
    public void testDownloadPaint() throws Exception {
        DownloadService downloadService = new DownloadService();
//        File downloadedFile = downloadService.downloadFileHttp(new File(System.getProperty("java.io.tmpdir") + "/" + "gene_association.goa_zebrafish")
//                , new URL("http://www.geneontology.org/gene-associations/submission/paint/pre-submission/gene_association.paint_zfin.gz")
//                , true);
        File downloadedFile = downloadService.downloadFile(new File(System.getProperty("java.io.tmpdir") + "/" + "gene_association.paint_zebrafish")
                , new URL("http://www.geneontology.org/gene-associations/submission/paint/pre-submission/gene_association.paint_zfin.gz")
        ,false
        );
            logger.error("download file to: " + downloadedFile);
        assertTrue(downloadedFile.exists());
//        assertTrue(downloadedFile.delete());
    }
}
