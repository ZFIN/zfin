package org.zfin.sequence.blast;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;

import java.io.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests blast file generation from downloads file.
 * Specifically tests BlastDownloadService.
 */
public class BlastDownloadsTest extends AbstractDatabaseTest{

    private Logger logger = LogManager.getLogger(BlastDownloadsTest.class);

    @Before
    public void setUp() {
    }

    // the test should be to write it out to a temp file and reread it without error
    @Test
    public void blastMorpholinoDownload() throws Exception{
        String fastaFile = BlastDownloadService.getMorpholinoDownload();
        assertNotNull(fastaFile) ;
        assertTrue(fastaFile.length()>1000) ;
        File tempFile = File.createTempFile("morpholino",".fa") ;
        logger.info("writing to file: "+ tempFile);
		tempFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(fastaFile);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile)) ;
        RichSequenceIterator iterator = RichSequence.IOTools.readFastaDNA(bufferedReader,null) ;
        assertNotNull(iterator) ;
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.nextRichSequence());
        assertTrue(iterator.hasNext());
    }

    @Test
    public void blastTalenDownload() throws Exception{
        String fastaFile = BlastDownloadService.getTalenDownload();
        assertNotNull(fastaFile) ;
        assertTrue(fastaFile.length()>1000) ;
        File tempFile = File.createTempFile("talen",".fa") ;
        logger.info("writing to file: "+ tempFile);
        tempFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(fastaFile);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile)) ;
        RichSequenceIterator iterator = RichSequence.IOTools.readFastaDNA(bufferedReader,null) ;
        assertNotNull(iterator) ;
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.nextRichSequence());
        assertTrue(iterator.hasNext());
    }


    @Test
    public void blastCrisprDownload() throws Exception{
        String fastaFile = BlastDownloadService.getCrisprDownload();
        assertNotNull(fastaFile) ;
        assertTrue(fastaFile.length()>1000) ;
        File tempFile = File.createTempFile("crispr",".fa") ;
        logger.info("writing to file: "+ tempFile);
        tempFile.deleteOnExit();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(fastaFile);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile)) ;
        RichSequenceIterator iterator = RichSequence.IOTools.readFastaDNA(bufferedReader,null) ;
        assertNotNull(iterator) ;
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.nextRichSequence());
        assertTrue(iterator.hasNext());
    }

    // the test should be to write it out to a temp file and reread it without error
    @Test
    public void genbankDownloadAll() throws Exception{
        String accessions = BlastDownloadService.getGenbankAllDownload();
        assertTrue(accessions.contains("AY627769"));
    }

    // a good but a 30 second test
    @Test
    public void getGenbankXpatCdnaDownload(){
        String results = BlastDownloadService.getGenbankXpatCdnaDownload();
        assertNotNull(results);
        logger.info("results length: "+ results.length());
        // 41K * 5 or so = 200000
        assertTrue(results.length()>200000);
    }

    @Test
    public void getGenomicRefseqDownload(){
        String accessions = BlastDownloadService.getGenomicRefseqDownload() ;
        assertNotNull(accessions);
        logger.info("results length: "+ accessions.length());
        // seems to be empty

        assertTrue(accessions.length() > 10);
    }


    @Test
    public void getGenomicGenbankDownload(){
        String accessions = BlastDownloadService.getGenomicGenbankDownload() ;
        assertNotNull(accessions);
        logger.info("results length: "+ accessions.length());
        // 17K * 5 or so = 850000
        assertTrue(accessions.length()>85000);
    }

}
