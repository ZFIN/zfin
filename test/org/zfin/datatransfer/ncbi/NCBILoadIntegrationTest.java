package org.zfin.datatransfer.ncbi;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDangerousDatabaseTest;
import org.zfin.framework.HibernateUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.*;

/**
 * Run tests against a database that only has test data in it.
 * Not to be run on prod or any other important database.
 * Can be invoked with:
 * docker compose run --rm -p 5005:5005 ncbiload bash -lc 'gradle -PgradleDebug -PncbiLoadTests test --tests org.zfin.datatransfer.ncbi.NCBILoadIntegrationTest '
 * The gradleDebug property will start the JVM in debug mode and listen on port 5005 for a debugger to attach. (optional)
 */
public class NCBILoadIntegrationTest extends AbstractDangerousDatabaseTest {

    public static final Boolean DELETE_ON_EXIT = true;
    public static final String ZDB_PUB_230516_87 = "ZDB-PUB-230516-87";

    private Path tempDir;
    private NCBILoadIntegrationTestHelper helper;

    /**
     * Test of the simplest case. Start with one gene with no NCBI link. The gene has an RNA sequence
     * that can be be matched to an NCBI record. After the load, there should be one new NCBI Gene ID link
     */
    @Test
    public void testInitialLoadCreatesOneLink() throws IOException {
        // Create database state before the load
        helper.beforeStateBuilder()
                .withGene("ZDB-GENE-010319-10", "id:ibd2600")
                .withDBLink("ZDB-GENE-010319-10", "BG985726", FDCONT_GEN_BANK_RNA, "ZDB-PUB-020723-5")
                .withGene2AccessionFile("7955\t80928\t-\tBG985726.1\t14389806\t-\t-\t-\t-\t-\t-\t?\t-\t-\t-\tid:ibd2600")
                .withZfGeneInfoFile("7955\t80928\tid:ibd2600\t-\t-\tZFIN:ZDB-GENE-010319-10|AllianceGenome:ZFIN:ZDB-GENE-010319-10\t5\t-\tid:ibd2600\tprotein-coding\tid:ibd2600\tid:ibd2600\tO\tuncharacterized protein LOC80928\t20250705\t-")
                .build();

        helper.runNCBILoad();


        // Verify database state
        assertEquals("Should create exactly one NCBI link", 1, helper.getNCBILinkCount("ZDB-GENE-010319-10"));

        List<String> ncbiIds = helper.getNCBILinks("ZDB-GENE-010319-10");
        assertEquals("Should have exactly one NCBI ID", 1, ncbiIds.size());
        assertEquals("Should link correct NCBI ID", "80928", ncbiIds.get(0));

        assertEquals("Should create exactly one attribution record", 1, helper.getAttributionCount("ZDB-GENE-010319-10"));

        // Verify output files
        NCBILoadIntegrationTestHelper.AfterState afterState = helper.getAfterState();
        assertEquals(true, afterState.getFile("before_load.csv").exists());
        assertEquals(true, afterState.getFile("after_load.csv").exists());
        assertEquals(1, afterState.getFile("before_load.csv").getDataLines().size());
        assertEquals(2, afterState.getFile("after_load.csv").getDataLines().size());

        assertDBLinkExists("ZDB-GENE-010319-10", "80928", FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_RNA);

    }

    /**
     * Test the case where a gene has an existing NCBI Gene ID link, but NCBI has replaced that ID with a new one.
     * After the load, the old NCBI Gene ID link should be replaced with the new one, and the GenBank
     * accession should also be linked to the new NCBI Gene ID.
     */
    @Test
    public void testGeneWithReplacedNCBIGeneMatchingByEnsembl() throws IOException {
        // Create database state before the load
        helper.beforeStateBuilder()
                .withGene("ZDB-GENE-120709-33", "si:ch211-209j12.2")
                .withDBLink("ZDB-GENE-120709-33", "103910949", FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT)
                .withDBLink("ZDB-GENE-120709-33", "ENSDARG00000099337", FDCONT_ENSDARG, "ZDB-PUB-200123-1")
                .withGene2AccessionFile("7955\t108183900\t-\tGDQQ01002583.1\t-\t-\t-\t-\t-\t-\t-\t?\t-\t-\t-\tsi:ch211-209j12.2")
                .withZfGeneInfoFile("7955\t108183900\tsi:ch211-209j12.2\t-\t-\tZFIN:ZDB-GENE-120709-33|Ensembl:ENSDARG00000099337|AllianceGenome:ZFIN:ZDB-GENE-120709-33\t4\t-\tsi:ch211-209j12.2\tncRNA\tsi:ch211-209j12.2\tsi:ch211-209j12.2\tO\tuncharacterized protein LOC108183900\t20250909\t-")
                .build();

        helper.runNCBILoad();

        NCBILoadIntegrationTestHelper.AfterState afterState = helper.getAfterState();
        assertEquals(2, afterState.getFile("before_load.csv").getDataLines().size());
        assertEquals(3, afterState.getFile("after_load.csv").getDataLines().size());

        //Check that the old NCBI Gene ID was replaced with the new one
        assertNcbiDBLinkDoesNotExist("ZDB-GENE-120709-33", "103910949");
        assertNcbiDBLinkExists("ZDB-GENE-120709-33", "108183900");
        assertDBLinkExists("ZDB-GENE-120709-33", "GDQQ01002583", FDCONT_GEN_BANK_RNA, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT);
    }

    @Test
    public void testGeneWithVegaLink() throws IOException {
        // Create database state before the load
        helper.beforeStateBuilder()
                .withGene("ZDB-GENE-040724-74", "si:dkey-192d15.2")
                .withVega("ZDB-GENE-040724-74", "OTTDARG00000004288", "si:dkey-192d15.2-201")
                .withGene2VegaFile("7955\t107980443\tOTTDARG00000004288\tNM_001327832.1\tOTTDART00000004513\tNP_001314761.1\tOTTDARP00000004104")
                .build();

        helper.runNCBILoad();

        NCBILoadIntegrationTestHelper.AfterState afterState = helper.getAfterState();
        assertEquals(1, afterState.getFile("before_load.csv").getDataLines().size());
        assertEquals(2, afterState.getFile("after_load.csv").getDataLines().size());

        // Expect to now have a NCBI Gene ID link of 107980443 based on the Vega mapping
        assertDBLinkExists("ZDB-GENE-040724-74", "107980443", FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_VEGA);
    }

    /**
     * Test the case where a gene has an existing Ensembl link and a Vega link, and NCBI has an entry
     * This means our logic should confirm a match based on the Ensembl link, but also based on the Vega link
     * In this case, we retain the attribution for the ensembl match, but not the attribution for the Vega match
     * In the future, we may want to keep both attributions
     *
     * @throws IOException
     */
    @Test
    public void testEnsemblMatchWithSupportingVegaMatch() throws IOException {
        // Create database state before the load
        helper.beforeStateBuilder()
                .withGene("ZDB-GENE-120703-25", "si:dkey-71l4.3")
                .withDBLink("ZDB-GENE-120703-25", "ENSDARG00000099112", FDCONT_ENSDARG, "ZDB-PUB-030703-1")
                .withVega("ZDB-GENE-120703-25", "OTTDARG00000039000", "si:dkey-71l4.3-201")
                .withGene2VegaFile("108183518", "OTTDARG00000039000")
                .withZfGeneInfoFile("108183518", "si:dkey-71l4.3",
                        List.of("ZFIN:ZDB-GENE-120703-25", "Ensembl:ENSDARG00000099112")
                )
                .build();

        helper.runNCBILoad();

        NCBILoadIntegrationTestHelper.AfterState afterState = helper.getAfterState();
        assertEquals(2, afterState.getFile("before_load.csv").getDataLines().size());
        assertEquals(3, afterState.getFile("after_load.csv").getDataLines().size());

        assertDBLinkExists("ZDB-GENE-120703-25", "108183518", FDCONT_NCBI_GENE_ID, PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT);
    }

    @Test
    public void testLostGeneID() throws IOException {
        // Create database state before the load
        helper.beforeStateBuilder()
                .withGene("ZDB-GENE-110913-43", "testgene1234")
                .withMarkerAnnotationStatus("ZDB-GENE-110913-43", "Current")
                .withDBLink("ZDB-GENE-110913-43", "12345678", FDCONT_NCBI_GENE_ID, "ZDB-PUB-020723-3")
                .build();

        helper.runNCBILoad();

        assertNcbiDBLinkDoesNotExist("ZDB-GENE-110913-43", "12345678");
        assertMarkerAnnotationStatus("ZDB-GENE-110913-43", "Unknown");
    }

    @Test
    public void testHandlingOf1toNwarning() throws IOException {
        //debug10:ZDB-GENE-030131-3603	101885800:AI794605 324880:AI883911

        //ZDB-GENE-030131-3603,324880,uncurated: NCBI gene load 2025-05-09 19:43:14.214854-07,ZDB-DBLINK-250509-141381,324880,,ZDB-FDBCONT-040412-1,ZDB-PUB-020723-3,3,13
        //ZDB-GENE-030131-3603,AI794605,uncurated: NCBI gene load 2023-11-24 19:38:28.40974-08,ZDB-DBLINK-231124-1486,AI794605,510,ZDB-FDBCONT-040412-37,ZDB-PUB-020723-5,3,13
        //ZDB-GENE-030131-3603,AI883911,uncurated: NCBI gene load 2023-11-24 19:38:28.40974-08,ZDB-DBLINK-231124-1642,AI883911,483,ZDB-FDBCONT-040412-37,ZDB-PUB-020723-5,3,13
        helper.beforeStateBuilder()
                .withGene("ZDB-GENE-030131-3603", "wu:fc46e01a")
                .withDBLink("ZDB-GENE-030131-3603", "324880", FDCONT_NCBI_GENE_ID, "ZDB-PUB-020723-3")
                .withDBLink("ZDB-GENE-030131-3603", "AI794605", FDCONT_GEN_BANK_RNA, "ZDB-PUB-020723-5")
                .withDBLink("ZDB-GENE-030131-3603", "AI883911", FDCONT_GEN_BANK_RNA, "ZDB-PUB-020723-5")
                .withGene2AccessionFile("7955\t324880\t-\tAI883911.1\t5589086\t-\t-\t-\t-\t-\t-\t?\t-\t-\t-\twu:fc46e01")
                .withGene2AccessionFile("7955\t101885800\t-\tAI794605.1\t5342321\t-\t-\t-\t-\t-\t-\t?\t-\t-\t-\tLOC101885800")
                .withZfGeneInfoFile("7955\t324880\twu:fc46e01\t-\tfc46e01\tZFIN:ZDB-GENE-030131-3603|AllianceGenome:ZFIN:ZDB-GENE-030131-3603\t4\t-\twu:fc46e01\tunknown\twu:fc46e01\twu:fc46e01\tO\t-\t20250519\t-")
                .withZfGeneInfoFile("7955\t101885800\tLOC101885800\t-\t-\t-\t4\t-\tuncharacterized LOC101885800\tprotein-coding\t-\t-\t-\tuncharacterized protein LOC101885800\t20250909\t-")
                .build();

        assertNcbiDBLinkExists("ZDB-GENE-030131-3603", "324880");

        helper.runNCBILoad();

        NCBILoadIntegrationTestHelper.AfterState afterState = helper.getAfterState();
        assertEquals(3, afterState.getFile("before_load.csv").getDataLines().size());
        assertEquals(2, afterState.getFile("after_load.csv").getDataLines().size());

        // Make sure the one-to-n report flags the issue
        assertTrue(afterState.getFile("reportOneToN").matches("ZDB-GENE-030131-3603.*wu:fc46e01a.*AI794605.*AI883911.*"));
        assertTrue(afterState.getFile("debug10").matches("ZDB-GENE-030131-3603.*101885800:AI794605.*324880:AI883911.*"));

        assertNcbiDBLinkDoesNotExist("ZDB-GENE-030131-3603", "324880");
    }

    @Test
    public void testNotInCurrentRelease() throws IOException {
        // Create database state before the load
        helper.beforeStateBuilder()
                .withGene("ZDB-GENE-010319-10", "id:ibd2600")
                .withDBLink("ZDB-GENE-010319-10", "80928", FDCONT_NCBI_GENE_ID, "ZDB-PUB-020723-5")
                .withNotInCurrentReleaseFile("80928")
                .build();

        helper.runNCBILoad();

        assertMarkerAnnotationStatus("ZDB-GENE-010319-10", "Not in current annotation release");
    }

    private void assertMarkerAnnotationStatus(String geneZdbID, String status) {
        String statusInDB = helper.getMarkerAnnotationStatus(geneZdbID);
        assertEquals(status, statusInDB);
    }

    public void assertDBLinkExists(String geneZdbID, String accessionNumber, String fdcontID, String publicationID) {
        helper.getDBLinksWithAttributions(geneZdbID, accessionNumber, fdcontID, publicationID);
        assertEquals(1, helper.getDBLinksWithAttributions(geneZdbID, accessionNumber, fdcontID, publicationID).size());
    }

    public void assertNcbiDBLinkExists(String geneZdbID, String ncbiID) {
        List<String> links = helper.getNCBILinks(geneZdbID, ncbiID);
        assertEquals("NCBI DBLink " + ncbiID + " should exist for gene " + geneZdbID, 1, links.size());
    }

    public void assertNcbiDBLinkDoesNotExist(String geneZdbID, String ncbiID) {
        List<String> links = helper.getNCBILinks(geneZdbID, ncbiID);
        assertEquals("NCBI DBLink " + ncbiID + " should not exist for gene " + geneZdbID, 0, links.size());
    }

    @Before
    public void setupTestData() throws IOException {
        //Make sure we are running in the NCBI test environment
        String isNcbiLoadContainer = System.getenv("IS_NCBI_LOAD_CONTAINER");
        if (!"true".equals(isNcbiLoadContainer)) {
            System.out.println("IS_NCBI_LOAD_CONTAINER environment variable is not set to true. Preventing run to avoid data corruption.");
            System.out.flush();
            throw new RuntimeException("NCBI_LOAD_CONTAINER environment variable is not set. Preventing run to avoid data corruption.");
        }

        tempDir = Files.createTempDirectory("ncbi_test_");
        helper = new NCBILoadIntegrationTestHelper(tempDir);
        if (DELETE_ON_EXIT) {
            tempDir.toFile().deleteOnExit();
        }
        helper.createTestFiles();
    }

    /**
     * Clean up resources after each test to prevent resource leakage between tests
     */
    @After
    public void tearDown() throws IOException {
        try {
            // Clear any system properties that might affect subsequent tests
            System.clearProperty("WORKING_DIR");
            System.clearProperty("NO_SLEEP");
            System.clearProperty("SKIP_DOWNLOADS");
            System.clearProperty("LOAD_NCBI_ONE_WAY_GENES");
            System.clearProperty("DB_NAME");
            System.clearProperty("SKIP_COMPRESS_ARTIFACTS");

            // Force cleanup of temp directory if it still exists
            if (tempDir != null && Files.exists(tempDir)) {
                try {
                    // Delete all files in temp directory
                    Files.walk(tempDir)
                            .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                            .forEach(path -> {
//                            try {
//                                Files.deleteIfExists(path);
//                            } catch (IOException e) {
//                                // Log but don't fail the test cleanup
//                                System.err.println("Warning: Could not delete " + path + ": " + e.getMessage());
//                            }
                            });
                } catch (IOException e) {
                    System.err.println("Warning: Could not fully clean up temp directory " + tempDir + ": " + e.getMessage());
                }
            }

            // Reset database transaction state
            if (HibernateUtil.currentSession().getTransaction().isActive()) {
                HibernateUtil.currentSession().getTransaction().rollback();
            }

        } catch (Exception e) {
            // Don't let cleanup failures break the test suite
            System.err.println("Warning: Error during test cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
