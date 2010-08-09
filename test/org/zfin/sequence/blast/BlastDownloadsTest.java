package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.getSessionFactory;

/**
 * Tests blast file generation from downloads file.
 * Specifically tests BlastDownloadService.
 */
public class BlastDownloadsTest extends AbstractDatabaseTest{

    private Logger logger = Logger.getLogger(BlastDownloadsTest.class);

    @Before
    public void setUp() {
        RepositoryFactory.getBlastRepository().setAllDatabaseLock(false);
    }

    @After
    public void closeSession() {
        RepositoryFactory.getBlastRepository().setAllDatabaseLock(false);
        HibernateUtil.closeSession();
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
        assertEquals(0,accessions.length());
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
