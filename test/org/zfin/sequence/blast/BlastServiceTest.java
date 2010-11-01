package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.sequence.Defline;
import org.zfin.sequence.Sequence;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class BlastServiceTest {

    @Test
    public void useEfetchForProtein(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("P26630") ;
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
    public void useEfetchForProtein2(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("NP_571379") ;
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1,sequences.size());
        Sequence sequence = sequences.get(0) ;
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("NP_571379"));
        assertTrue(sequence.getFormattedData().length()>100);
        assertTrue(sequence.getFormattedSequence().length()>100);
        assertTrue(defline.toString().length()>20);
    }

    @Test
    public void useEfetchForNucleotide(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("AY627769") ;
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
    public void useEfetchForNucleotide2(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("FN428721") ;
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1,sequences.size());
        Sequence sequence = sequences.get(0) ;
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("FN428721"));
        assertTrue(sequence.getFormattedData().length()>100);
        assertTrue(sequence.getFormattedSequence().length()>100);
        assertTrue(defline.toString().length()>20);
    }

    @Test
    public void useEfetchForNucleotideWithMultipleReturn(){
        List<Sequence> sequences = NCBIEfetch.getSequenceForAccession("X63183") ;
        assertTrue(CollectionUtils.isNotEmpty(sequences));
        assertEquals(1,sequences.size());
        Sequence sequence = sequences.get(0) ;
        Defline defline = sequence.getDefLine();
        assertTrue(defline.getAccession().equals("X63183"));
        assertTrue(sequence.getFormattedData().length()>100);
        assertTrue(sequence.getFormattedSequence().length()>100);
        assertTrue(defline.toString().length()>20);
    }

    @Test
    public void validateAccessions(){
        String[] accessions = {"NP_571379", "AY627769", "FN428721", "X63183"} ;
        for(String accession : accessions){
            assertTrue(NCBIEfetch.validateAccession(accession)) ;
        }
    }

}
