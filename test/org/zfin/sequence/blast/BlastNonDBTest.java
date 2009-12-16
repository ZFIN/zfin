package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.sequence.Defline;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


/**
 * This tests handles the ExecuteBlast code.
 */
public class BlastNonDBTest {

    private final Logger logger = Logger.getLogger(BlastNonDBTest.class) ;

    private final String seq1  = ">ga17\n" +
            "  GCACAGATAAAAATCCACGCTCGCA\n" +
            " >hlx1\n" +
            "  AGCCGAACAATACGCAGTCCACAGG\n" +
            " >meis2.2\n" +
            "  CTGTGTCGTAGATTTAATTTCCCAG\n" +
            " >nostrin\n" +
            "  GTCCTTCATCTTCACTCACGCTGGT\n" ;


    private final String seq2  = ">ga17\n" +
            "  1 GCACAGATAAAAATCCACGCTCGCA\n" +
            " >hlx1\n" +
            "  1 AGCCGAACAATACGCAGTCCACAGG\n" +
            " >meis2.2\n" +
            "  1 CTGTGTCGTAGATTTAATTTCCCAG\n" +
            " >nostrin\n" +
            "  1 GTCCTTCATCTTCACTCACGCTGGT\n" ;

    private final String seq1And2Result  = ">ga17\n" +
            "GCACAGATAAAAATCCACGCTCGCA\n" +
            ">hlx1\n" +
            "AGCCGAACAATACGCAGTCCACAGG\n" +
            ">meis2.2\n" +
            "CTGTGTCGTAGATTTAATTTCCCAG\n" +
            ">nostrin\n" +
            "GTCCTTCATCTTCACTCACGCTGGT\n" ;

    private final String seq3  = ">ga17\n" +
            "  1 GCACAGATAAAAATCCACGCTCGCA\n" +
            "  2 AGCCGAACAATACGCAGTCCACAGG\n" +
            " >meis2.2\n" +
            "  1 CTGTGTCGTAGATTTAATTTCCCAG\n" +
            "  2 GTCCTTCATCTTCACTCACGCTGGT\n" ;

    private final String seq3result  = ">ga17\n" +
            "GCACAGATAAAAATCCACGCTCGCA\n" +
            "AGCCGAACAATACGCAGTCCACAGG\n" +
            ">meis2.2\n" +
            "CTGTGTCGTAGATTTAATTTCCCAG\n" +
            "GTCCTTCATCTTCACTCACGCTGGT\n" ;

    @Before
    public void setUp() {
        TestConfiguration.initApplicationProperties();
    }

    @Test
    public void useEfetchForProtein(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("P26630",false) ;
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1,sequences.size());
        Sequence sequence = sequences.get(0) ;
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("P26630"));
        assertTrue(sequence.getFormattedData().length()>100);
        assertTrue(sequence.getFormattedSequence().length()>100);
        assertTrue(defline.toString().length()>20);
    }

    @Test
    public void useEfetchForNucleotide(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("AY627769",true) ;
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1,sequences.size());
        Sequence sequence = sequences.get(0) ;
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("AY627769"));
        assertTrue(sequence.getFormattedData().length()>100);
        assertTrue(sequence.getFormattedSequence().length()>100);
        assertTrue(defline.toString().length()>20);
    }

    @Test
    public void useEfetchForNucleotideWithMultipleReturn(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("X63183",true) ;
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1,sequences.size());
        Sequence sequence = sequences.get(0) ;
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("X63183"));
        assertTrue(sequence.getFormattedData().length()>100);
        assertTrue(sequence.getFormattedSequence().length()>100);
        assertTrue(defline.toString().length()>20);
    }


    /**
     * todo: does not really need to be in a database class, but really isn't a databasepresentation service, either.
     */
    @Test
    public void polyATest(){
        MountedWublastBlastService service = MountedWublastBlastService.getInstance() ;
        assertEquals("T",service.clipPolyATail("TAAAAAA")) ;
        assertEquals("TAAAAAAT",service.clipPolyATail("TAAAAAAT")) ;
        assertEquals("TTAAAAA",service.clipPolyATail("TTAAAAA")) ;
        assertEquals("TT",service.clipPolyATail("TTAAAAAAAA")) ;
        assertEquals("",service.clipPolyATail("AAAAAAAA")) ;
    }

    @Test
    public void setBlastResultFile(){
        XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
        try {
            MountedWublastBlastService.getInstance().setBlastResultFile(xmlBlastBean);
            assertNotNull(xmlBlastBean.getResultFile());
            assertTrue(xmlBlastBean.getResultFile().getName().startsWith("blast"));
            assertTrue(xmlBlastBean.getResultFile().getName().endsWith(".xml"));
        } catch (IOException e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void removeLeadingNumbersFromSequences(){
        assertEquals(seq1And2Result,MountedWublastBlastService.getInstance().removeLeadingNumbers(seq1)) ;
        assertEquals(seq1And2Result,MountedWublastBlastService.getInstance().removeLeadingNumbers(seq2)) ;
        assertEquals(seq3result,MountedWublastBlastService.getInstance().removeLeadingNumbers(seq3)) ;
    }

}