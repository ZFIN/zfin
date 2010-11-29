package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.presentation.XMLBlastViewController;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import static org.junit.Assert.*;


/**
 * This tests handles the ExecuteBlast code.
 */
public class BlastNonDBTest {

    private final Logger logger = Logger.getLogger(BlastNonDBTest.class) ;

    private MountedWublastBlastService service = MountedWublastBlastService.getInstance() ;

    private final String seq1  = ">ga17\n" +
            "  GCACAGATAAAAATCCACGCTCGCA\n" +
            " >hlx1\n" +
            "  AGCCGAACAATACGCAGTCCACAGG\n" +
            " >meis2.2\n" +
            "  CTGTGTCGTAGATTTAATTTCCCAG\n" +
            " >nostrin\n" +
            "  GTCCTTCATCTTCACTC ACGCTGGT\n" ;


    private final String seq2  = ">ga17\n" +
            "  1 GCACAGATAAAAATCCACGCTCGCA\n" +
            " >hlx1\n" +
            "  1 AGCCGAACAATACGCAGTCCACAGG\n" +
            " >meis2.2\n" +
            "  1 CTGTGTCGTAGATTTAATTTCCCAG\n" +
            " >nostrin\n" +
            "  1 GTCCTTCATCTTCACTCA CGCTGGT\n" ;

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
            "  2 GTCCTTCATCTTCAC   TCACGCTGGT\n" ;

    private final String seq3result  = ">ga17\n" +
            "GCACAGATAAAAATCCACGCTCGCA\n" +
            "AGCCGAACAATACGCAGTCCACAGG\n" +
            ">meis2.2\n" +
            "CTGTGTCGTAGATTTAATTTCCCAG\n" +
            "GTCCTTCATCTTCACTCACGCTGGT\n" ;


    private final String seq4 = ">ga17 CAATATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATAC ACACTCCCTACATACGATATATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATAC ACACTCCCTACATACGATA" ;
    private final String seq4result = ">ga17\nCAATATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATACACACTCCCTACATACGATATATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATACACACTCCCTACATACGATA\n" ;

    private final String seq5 = ">ga17 CAATATAGATAGATAGATAgumkryvbhdwsn-ACACACTCCCTACATACGATAT ATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATAC ACACTCCCTACATACGATA" ;
    private final String seq5result = ">ga17\nCAATATAGATAGATAGATAgumkryvbhdwsn-ACACACTCCCTACATACGATATATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATACACACTCCCTACATACGATA\n" ;

    private final String seq6 = ">ga17 CAATATAGATAGATAGATAGATATACWBZJXlsefavgktrpdiqnfyhmcwbzjx-*ACAC   TCCCTACATACGATATATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATAC ACACTCCCTACATACGATA" ;
    private final String seq6result = ">ga17\nCAATATAGATAGATAGATAGATATACWBZJXlsefavgktrpdiqnfyhmcwbzjx-*ACACTCCCTACATACGATATATAGATAGATAGATAGATATATAGAGATAGATATATAGATATATAGTAGATATACACACTCCCTACATACGATA\n" ;

    @Test
    public void removeSpacesFromSequence(){
        String inputString = "TTGTACATTACTTTGTATTT ATTATATCAGTTAAT" ;
        String outputString = "TTGTACATTACTTTGTATTTATTATATCAGTTAAT\n" ;
        assertEquals(outputString,service.removeLeadingNumbers(inputString,XMLBlastBean.SequenceType.NUCLEOTIDE));
    }


    /**
     * todo: does not really need to be in a database class, but really isn't a databasepresentation service, either.
     */
    @Test
    public void polyATest(){
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
            service.setBlastResultFile(xmlBlastBean);
            assertNotNull(xmlBlastBean.getResultFile());
            assertTrue(xmlBlastBean.getResultFile().getName().startsWith("blast"));
            assertTrue(xmlBlastBean.getResultFile().getName().endsWith(".xml"));
        } catch (IOException e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void removeLeadingNumbersFromSequences(){
        assertEquals(seq1And2Result,service.removeLeadingNumbers(seq1,XMLBlastBean.SequenceType.NUCLEOTIDE)) ;
        assertEquals(seq1And2Result,service.removeLeadingNumbers(seq2,XMLBlastBean.SequenceType.NUCLEOTIDE)) ;
        assertEquals(seq3result,service.removeLeadingNumbers(seq3,XMLBlastBean.SequenceType.NUCLEOTIDE)) ;
    }

    @Test
    public void fixDeflineValue(){

        String[] strings = service.fixDeflineNucleotideSequence(seq4) ;
        assertEquals(2,strings.length);
        assertEquals(seq4result,service.removeLeadingNumbers(seq4,XMLBlastBean.SequenceType.NUCLEOTIDE));

        strings = service.fixDeflineNucleotideSequence(seq5) ;
        assertEquals(2,strings.length);
        assertEquals(seq5result,service.removeLeadingNumbers(seq5,XMLBlastBean.SequenceType.NUCLEOTIDE));

        Matcher m = AbstractWublastBlastService.proteinSequencePattern.matcher(seq6) ;
        assertTrue(m.find()) ; 

        strings = service.fixDeflineProteinSequence(seq6) ;
        assertEquals(2,strings.length);
        assertEquals(seq6result,service.removeLeadingNumbers(seq6,XMLBlastBean.SequenceType.PROTEIN));
    }

    @Test
    public void testString(){
        String testString = "/private/apps/wublast/blastn /research/zblastfiles/zmore/ogodb/Current/vega_zfin /research/zblastfiles/zmore/ogodb/dump541174143337370754.fa -novalidctxok -nonnegok -gapall -restest  E=1.0E-25 Q=7 R=2 kap cpus=1        M=1 N=-3 W=12    S2=14 gapS2=19 X=6 gapX=15 gapW=12    -gi  gapL=1.3741 gapK=0.711 gapH=1.3073   mformat=7<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE BlastOutput PUBLIC \"-//NCBI//NCBI BlastOutput/EN\" \"NCBI_BlastOutput.dtd\">\n" +
                "<!-- For more compact output, use the xmlcompact option -->\n" +
                "<BlastOutput>\n" +
                "</BlastOutput>" ;
        assertFalse(testString.startsWith("<BlastOutput>"));
        XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
        xmlBlastBean.setDataLibraryString("stuff");
        String fixedString = SMPWublastService.getInstance().fixBlastXML(testString,xmlBlastBean) ;
        assertTrue(fixedString.startsWith("<BlastOutput>"));
    }

    @Test
    public void testBlastNaming(){
        XMLBlastBean xmlBlastBean = new XMLBlastBean();
        String ticketNumber = "6347900181946270349";
        xmlBlastBean.setResultFile(new File(ticketNumber));
        assertEquals(ticketNumber,xmlBlastBean.getTicketNumber()) ;
        xmlBlastBean.setResultFile(new File(XMLBlastBean.BLAST_PREFIX + ticketNumber)) ;
        assertEquals(ticketNumber,xmlBlastBean.getTicketNumber()) ;
        xmlBlastBean.setResultFile(new File(XMLBlastBean.BLAST_PREFIX + ticketNumber+ XMLBlastBean.BLAST_SUFFIX)) ;
        assertEquals(ticketNumber,xmlBlastBean.getTicketNumber()) ;
        xmlBlastBean.setResultFile(new File(ticketNumber+ XMLBlastBean.BLAST_SUFFIX)) ;
        assertEquals(ticketNumber,xmlBlastBean.getTicketNumber()) ;
    }

    @Test
    public void fixFileName(){
        XMLBlastBean xmlBlastBean = new XMLBlastBean();
        XMLBlastViewController xmlBlastViewController = new XMLBlastViewController();
        String ticketNumber = "6347900181946270349";
        xmlBlastBean.setResultFile(new File(ticketNumber));
        assertFalse(xmlBlastViewController.isValidBlastResultLocation(xmlBlastBean));
        xmlBlastBean = xmlBlastViewController.fixFileLocation(xmlBlastBean);
        try {
            if(xmlBlastBean.getResultFile().exists()){
                assertTrue(xmlBlastBean.getResultFile().delete());
            }
            assertTrue(xmlBlastBean.getResultFile().createNewFile());
        } catch (IOException e) {
            fail(e.toString()) ;
        }
        assertTrue(xmlBlastViewController.isValidBlastResultLocation(xmlBlastBean));
        assertTrue(xmlBlastBean.getResultFile().delete());
    }
}