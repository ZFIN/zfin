package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.datatransfer.webservice.EBIFetch;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.sequence.Defline;
import org.zfin.sequence.Sequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 */
public class WebServiceSoapClientTest {

    private Logger logger = Logger.getLogger(WebServiceSoapClientTest.class);

    @Test
    public void useEfetchForProtein() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("P26630");
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("P26630"));
        assertTrue(sequence.getFormattedData().length() > 100);
        assertTrue(sequence.getFormattedSequence().length() > 100);
        assertTrue(defline.toString().length() > 20);
    }

    @Test
    public void useEfetchForProtein2() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("NP_571379");
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("NP_571379"));
        assertTrue(sequence.getFormattedData().length() > 100);
        assertTrue(sequence.getFormattedSequence().length() > 100);
        assertTrue(defline.toString().length() > 20);
    }

//    @Test
    // this is curently borken at EBI
//    public void useEBIForUniprot(){
//        assertFalse(EBIFetch.validateAccession("NP_571379")) ;
//        assertTrue(EBIFetch.validateAccession("B3DJJ0")) ;
//    }


    @Test
    public void useEBIForUniprot() {
        assertFalse(EBIFetch.validateAccession("NP_571379"));
        assertTrue(EBIFetch.validateAccession("B3DJJ0"));
    }

    @Test
    public void useEfetchForNucleotide() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("AY627769");
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("AY627769"));
        assertTrue(sequence.getFormattedData().length() > 100);
        assertTrue(sequence.getFormattedSequence().length() > 100);
        assertTrue(defline.toString().length() > 20);
    }

    @Test
    public void useEfetchForNewSequence() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("JF828767");
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("JF828767"));
        assertTrue(sequence.getFormattedData().length() > 100);
        assertTrue(sequence.getFormattedSequence().length() > 100);
        assertTrue(defline.toString().length() > 20);
    }

    @Test
    public void useEfetchForBadSequence() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("notasequencethatIknowof");
        assertTrue(CollectionUtils.isEmpty(sequences));
        assertEquals(0, sequences.size());
    }

    @Test
    public void useEfetchForNucleotide2() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("FN428721");
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("FN428721"));
        assertTrue(sequence.getFormattedData().length() > 100);
        assertTrue(sequence.getFormattedSequence().length() > 100);
        assertTrue(defline.toString().length() > 20);
    }

    @Test
    public void useEfetchForNucleotideWithMultipleReturn() {
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("X63183");
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("X63183"));
        assertTrue(sequence.getFormattedData().length() > 100);
        assertTrue(sequence.getFormattedSequence().length() > 100);
        assertTrue(defline.toString().length() > 20);
    }

    @Test
    public void validateAccessions() {
        String[] accessions = {"NP_571379", "AY627769", "FN428721", "X63183"};
        for (String accession : accessions) {
            assertTrue(NCBIEfetch.validateAccession(accession));
        }
    }

    @Test
    public void hasMicroarray() {
        assertTrue(NCBIEfetch.hasMicroarrayData(new HashSet<String>(), "rpf1"));
        assertFalse(NCBIEfetch.hasMicroarrayData(new HashSet<String>(), "abcdefg"));

        // for ZDB-EST-010111-34 a rare clone with GEO expression
        Set<String> strings = new HashSet<String>();
        strings.add("AI584640");
        strings.add("AI545457");
        assertTrue(NCBIEfetch.hasMicroarrayData(strings));
        Set<String> strings2 = new HashSet<String>();
        strings.add("BBBBBBBB");
        assertFalse(NCBIEfetch.hasMicroarrayData(strings2));
    }

    @Test
    public void getLink() {
        List<String> accessions = new ArrayList<String>();
        accessions.add("AB");
        accessions.add("CD");
        String link = NCBIEfetch.createMicroarrayQuery(accessions, "dogz");
        assertEquals("txid7955[organism] AND (dogz[gene symbol] OR (AB OR AB.* OR CD OR CD.*))", link);
    }

    // just to see what platforms are there, not worth running otherwise
//    @Test
    public void getMicroArrayLink() throws Exception {
        Set<String> platforms = NCBIEfetch.getPlatformsForZebrafishMicroarrays();
        assertThat(platforms.size(), greaterThan(0));
        for (String platform : platforms) {
            logger.info("platform: " + platform);
        }
    }


}
