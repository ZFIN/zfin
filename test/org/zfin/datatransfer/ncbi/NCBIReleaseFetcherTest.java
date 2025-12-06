package org.zfin.datatransfer.ncbi;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.datatransfer.ncbi.dto.Gene2AccessionDTO;
import org.zfin.datatransfer.ncbi.dto.Gene2VegaDTO;
import org.zfin.datatransfer.ncbi.dto.GeneInfoDTO;
import org.zfin.datatransfer.ncbi.dto.RefSeqCatalogDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test class for FileUtil.
 */
public class NCBIReleaseFetcherTest {

    NCBIReleaseFetcher fetcher;

    @Before
    public void setUp() {
        fetcher = new NCBIReleaseFetcher();
        //for local testing, first start a http-server in a directory with the files:
        // gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz
        // gene/DATA/ARCHIVE/gene2vega.gz
        // gene/DATA/gene2accession.gz
        // refseq/release/release-catalog/RefSeq-release224.catalog.gz
        // refseq/release/RELEASE_NUMBER  (contents: 224)

//        fetcher.setUrlBase("http://127.0.0.1:8080");
    }

    @After
    public void tearDown() {

    }

    @Test
    public void getCurrentReleaseTest() {
        Optional<Integer> release = fetcher.getCurrentReleaseNumber();
        assertTrue(release.isPresent());

        //should be greater than 100
        assertTrue(release.get() > 100);
        System.out.println("Current release is: " + release.get());
    }

    @Test
    public void getReleaseUrlCannotConnectTest() {
        fetcher.setReleaseUrl("http://127.0.0.1/bogus/url/meant/to/fail");
        Optional<Integer> release = fetcher.getCurrentReleaseNumber();
        assertTrue(release.isEmpty());
    }

    @Test
    public void getReleaseUrlCannotParseTest() {
        fetcher.setReleaseUrl("https://www.google.com");
        Optional<Integer> release = fetcher.getCurrentReleaseNumber();
        assertTrue(release.isEmpty());
    }

    @Test
    public void getReleaseUrlFailsOnZeroTest() throws IOException {

        //temporary file that will be deleted after test using the jre temp directory
        Path file = Files.createTempFile(Path.of(System.getProperty("java.io.tmpdir")), "test", ".txt");         
        Files.writeString(file, "0");

        fetcher.setReleaseUrl(file.toUri().toString());
        Optional<Integer> release = fetcher.getCurrentReleaseNumber();
        Files.delete(file);
        assertTrue(release.isEmpty());
    }

    @Test
    @Ignore
    public void downloadGene2AccessionReleaseFileTest() throws IOException {
        File ncbiFile = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.GENE2ACCESSION, new File("/tmp/gene2accession.gz"), null);
        assertNotNull(ncbiFile);
        assertTrue(ncbiFile.exists());
    }


    @Test
    @Ignore
    public void downloadEachReleaseFileTest() throws IOException {
        File ncbiFile = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.GENE2ACCESSION, new File("/tmp/gene2accession.gz"), null);
        assertNotNull(ncbiFile);
        assertTrue(ncbiFile.exists());

        ncbiFile = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.GENE2VEGA, new File("/tmp/gene2vega.gz"), null);
        assertNotNull(ncbiFile);
        assertTrue(ncbiFile.exists());

        ncbiFile = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.ZF_GENE_INFO, new File("/tmp/zf_gene_info.gz"), null);
        assertNotNull(ncbiFile);
        assertTrue(ncbiFile.exists());

        ncbiFile = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.REFSEQ_CATALOG, new File("/tmp/RefSeqCatalog.gz"), 224);
        assertNotNull(ncbiFile);
        assertTrue(ncbiFile.exists());
    }

    @Test
    @Ignore
    public void downloadFullSetReleaseFilesTest() throws IOException {
        File targetDir = new File("/tmp/ncbi-dl-target-dir/");
        NCBIReleaseFileSet fileSet = fetcher.downloadReleaseFiles(targetDir, 224);
        assertNotNull(fileSet);
        assertTrue(fileSet.getGene2accession().exists());
        assertTrue(fileSet.getRefSeqCatalog().exists());
        assertTrue(fileSet.getGene2vega().exists());
        assertTrue(fileSet.getZfGeneInfo().exists());
        fileSet.deleteAllFiles();
        targetDir.delete();
    }

    @Test
    @Ignore
    public void downloadCatalogIsFilteredToDanioTest() throws IOException {
        Optional<Integer> num = fetcher.getCurrentReleaseNumber();
        assertTrue(num.isPresent());
        File file = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.REFSEQ_CATALOG, new File("/tmp/RefSeqCatalog.gz"), 224);
        assertTrue(file.exists());
        assertTrue(file.length() > 1000);
        assertTrue(file.length() < 10_000_000);
        file.delete();
    }

    @Test
    @Ignore
    public void downloadGene2accessionIsFilteredToDanioTest() throws IOException {
        Optional<Integer> num = fetcher.getCurrentReleaseNumber();
        assertTrue(num.isPresent());
        File file = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.GENE2ACCESSION, new File("/tmp/gene2accession.gz"), null);
        assertTrue(file.exists());
        assertTrue("File should be more than 1000 bytes after filter", file.length() > 1000);
        assertTrue("File should be less than 10MB after filter", file.length() < 30_000_000);
        file.delete();
    }

    @Test
    @Ignore
    public void readGene2accessionFileTest() throws IOException {
        File file = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.GENE2ACCESSION, new File("/tmp/gene2accession.gz"), null);

        NCBIReleaseFileReader fileReader = new NCBIReleaseFileReader();
        List<Gene2AccessionDTO> gene2AccessionData = fileReader.readGene2AccessionFile(file);
        assertNotNull(gene2AccessionData);
        assertTrue(gene2AccessionData.size() > 0);
        assertEquals("7955", gene2AccessionData.get(0).taxID());
//        assertEquals(220939, gene2AccessionData.size());
        gene2AccessionData.forEach( dto -> {
            assertEquals("7955", dto.taxID());
        });
    }


    @Test
    @Ignore
    public void readAllFilesTest() throws IOException {
        File ncbiFile1 = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.GENE2ACCESSION, new File("/tmp/gene2accession.gz"), null);
        File ncbiFile2 = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.GENE2VEGA, new File("/tmp/gene2vega.gz"), null);
        File ncbiFile3 = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.ZF_GENE_INFO, new File("/tmp/zf_gene_info.gz"), null);
        File ncbiFile4 = fetcher.downloadReleaseFile(NCBIReleaseFileSet.FileName.REFSEQ_CATALOG, new File("/tmp/RefSeq-release224.catalog.gz"), 224);

        NCBIReleaseFileReader fileReader = new NCBIReleaseFileReader();
        List<Gene2AccessionDTO> data1 = fileReader.readGene2AccessionFile(ncbiFile1);
        List<Gene2VegaDTO> data2 = fileReader.readGene2VegaFile(ncbiFile2);
        List<GeneInfoDTO> data3 = fileReader.readGeneInfoFile(ncbiFile3);
        List<RefSeqCatalogDTO> data4 = fileReader.readRefSeqCatalogFile(ncbiFile4);

        assertTrue(data1.size() > 100);
        assertTrue(data2.size() > 100);
        assertTrue(data3.size() > 100);
        assertTrue(data4.size() > 100);
    }

    @Test
    @Ignore
    public void readAllFilesAsSetTest() throws IOException {
        NCBIReleaseFileSet fileSet = fetcher.downloadReleaseFiles(new File("/tmp/ncbi-dl-target-dir/"), 224);
        NCBIReleaseFileReader fileReader = new NCBIReleaseFileReader();
        List<Gene2AccessionDTO> data1 = fileReader.readGene2AccessionFile(fileSet);
        List<Gene2VegaDTO> data2 = fileReader.readGene2VegaFile(fileSet);
        List<GeneInfoDTO> data3 = fileReader.readGeneInfoFile(fileSet);
        List<RefSeqCatalogDTO> data4 = fileReader.readRefSeqCatalogFile(fileSet);

        assertTrue(data1.size() > 100);
        assertTrue(data2.size() > 100);
        assertTrue(data3.size() > 100);
        assertTrue(data4.size() > 100);
    }

    @Test
    @Ignore
    public void getLatestReleaseFileSetTest() throws IOException {
        File downloadTo = new File("/tmp/ncbi-dl-target-dir/");
        NCBIReleaseFileReader reader = fetcher.downloadLatestReleaseFileSetReader(downloadTo);
        assertNotNull(reader);
        assertNotNull(reader.getFileset());
        System.out.println("Release number: " + reader.getFileset().getReleaseNumber());
        System.out.flush();
        assertEquals(224, reader.getFileset().getReleaseNumber());

        NCBIReleaseFileSet fileset = reader.getFileset();
        assertTrue(fileset.getGene2accession().exists());
        assertTrue(fileset.getRefSeqCatalog().exists());
        assertTrue(fileset.getGene2vega().exists());
        assertTrue(fileset.getZfGeneInfo().exists());

        assertTrue(reader.readGene2VegaFile().size() > 100);
        assertTrue(reader.readGene2AccessionFile().size() > 100);
        assertTrue(reader.readGeneInfoFile().size() > 100);
        assertTrue(reader.readRefSeqCatalogFile().size() > 100);

        File[] files = downloadTo.listFiles();
        assertNotNull(files);
        for(File file : files) {
            System.out.println("File: " + file.getName() + " size: " + file.length());
        }
        System.out.flush();
        fileset.deleteAllFiles();
    }

}
