package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.framework.presentation.tags.DeflineTag;
import org.zfin.gwt.marker.ui.SequenceValidator;

import java.util.List;

import static org.junit.Assert.*;

public class SequenceTest {

    private Logger logger = Logger.getLogger(SequenceTest.class);

    private String testString1 = "lcl|ZFINPROT0000000077|ZDB-TSCRIPT-081112-10003 publishedProtein ottdart00000025400 transcript   13 aa";
    private String testString3 = "lcl|ENSDART00000066225|gene:ENSDARG00000045045 cdna:known chromosome:ZFISH7:25:15179762:15200474:1 ";

    @Test
    public void replaceMarkerLink() {
        DeflineTag deflineTag = new DeflineTag();
        int index1 = testString1.indexOf("ZDB-TSCRIPT-081112-10003");
        String outputDefline = deflineTag.replaceMarkerWithLink(new String(testString1));
        logger.debug("output defline: " + outputDefline);

        int index2 =
                outputDefline.indexOf(
                        "<a href=/action/marker/view/ZDB-TSCRIPT-081112-10003>ZDB-TSCRIPT-081112-10003</a>");
        assertEquals("indexes should be equal", index1, index2);
    }


    @Test
    public void replaceEnsdargLink() {
        DeflineTag deflineTag = new DeflineTag();
        int index1 = testString3.indexOf("gene:ENSDARG00000045045");
        String outputDefline = deflineTag.replaceEndargsWithLink(new String(testString3));
        logger.debug("output defline: " + outputDefline);

        int index2 =
                outputDefline.indexOf(
                        "<a href=http://www.ensembl.org/Danio_rerio/geneview?gene=ENSDARG00000045045>gene:ENSDARG00000045045</a>"
                );
        assertEquals("indexes should be equal", index1, index2);
    }


    @Test
    public void validateNucletodieSequence() {
        assertEquals(0,
                SequenceValidator.validateNucleotideSequence("abcderfg"));
        assertEquals(SequenceValidator.NOT_FOUND,
                SequenceValidator.validateNucleotideSequence("ATCGUMKRYVBHDWSN-"));
        assertEquals(SequenceValidator.NOT_FOUND,
                SequenceValidator.validateNucleotideSequence("AAAAAATTTTGGGGGCCCCUUU"));
        assertEquals(5,
                SequenceValidator.validateNucleotideSequence("ATCGUZMKRYVBHDWSN-"));
    }

    @Test
    public void validatePolypeptideSequence() {
        assertEquals(0,
                SequenceValidator.validatePolypeptideSequence("abcderfg"));
        assertEquals(SequenceValidator.NOT_FOUND,
                SequenceValidator.validatePolypeptideSequence("LSEAVGKTRPDIQNFYHMCW*-"));
        assertEquals(19,
                SequenceValidator.validatePolypeptideSequence("AAAAAATTTTGGGGGCCCCUUU"));
        assertEquals(4,
                SequenceValidator.validatePolypeptideSequence("ATCGUZMKRYVBHDWSN-"));
    }

    @Test
    public void alignmentParse() {
        Hit hit = new Hit();
        String alignment = "Query:  1826 TCTTAATGTAATTTATTAGGTACGTTTTCATAAGAGAAAAATATTTATGTGTCCCACAAA 1767\n" +
                "             |||||||||||||||| |||||||||||||||||| ||||||||||||||||||||||||\n" +
                "Sbjct:     1 TCTTAATGTAATTTATAAGGTACGTTTTCATAAGAAAAAAATATTTATGTGTCCCACAAA 60\n" ;
        assertEquals(1826, hit.getQueryStart(alignment));
        assertEquals(1767, hit.getQueryEnd(alignment));
        assertEquals(1, hit.getSubjectStart(alignment));
        assertEquals(60, hit.getSubjectEnd(alignment));

    }

    @Test
    public void alignmentColor() {
        Hit hit = new Hit();
        hit.setAlignment("Query:  1826 TCTTAATGTAATTTATTAGGTACGTTTTCATAAGAGAAAAATATTTATGTGTCCCACAAA 1767\n" +
                "             |||||||||||||||| |||||||||||||||||| ||||||||||||||||||||||||\n" +
                "Sbjct:     1 TCTTAATGTAATTTATAAGGTACGTTTTCATAAGAAAAAAATATTTATGTGTCCCACAAA 60\n");

        assertTrue(hit.isReversed());

        hit.setAlignment("Query:  1767 TCTTAATGTAATTTATTAGGTACGTTTTCATAAGAGAAAAATATTTATGTGTCCCACAAA 1826\n" +
                "             |||||||||||||||| |||||||||||||||||| ||||||||||||||||||||||||\n" +
                "Sbjct:     1 TCTTAATGTAATTTATAAGGTACGTTTTCATAAGAAAAAAATATTTATGTGTCCCACAAA 60\n");

        assertFalse(hit.isReversed());
    }

    @Test
    public void hsps() {
        String alignment = "Query:  1334 ACATGTGAAACTTACTCTTAATCACATTTGATGATAATTGCT-GATACTAG 1383\n" +
                "             |||| | ||||| | |||| ||||||||||| ||| |||  | ||| | ||\n" +
                "Sbjct:  3663 ACATATAAAACTCATTCTTGATCACATTTGAAGATGATTTTTTGATCCAAG 3713\n" +
                "\n" +
                " Score = 820 (129.1 bits), Expect = 2.9e-62, Sum P(2) = 2.9e-62\n" +
                " Identities = 438/698 (62%), Positives = 438/698 (62%), Strand = Plus / Plus\n" +
                "\n" +
                "Query:   447 ATCTTCCCAGTCGATTGACACCTGTTATGATTTGCACTTAAA-ATAT-TACCACTTACTA 390\n" +
                "             || |||  |   ||| |||| |    | |  || |  ||| | |||| | | | ||||| \n" +
                "Sbjct:  2758 ATGTTCTGATATGATAGACAACAAGAAAGTATTCCTTTTATACATATATTCTA-TTACTT 2816\n";

        Hit hit = new Hit();
        hit.setAlignment(alignment);

        String[] hsps = hit.getHsps(alignment);
        assertEquals(2,hsps.length);
        assertFalse(hit.isReversed(hsps[0])) ;
        assertTrue(hit.isReversed(hsps[1])) ;
        String formattedAlignment = hit.getFormattedAlignment();

        String[] alignments = formattedAlignment.split("<pre");
        assertEquals(4,alignments.length);

        List<String> descriptions = hit.getDescriptions(alignment) ;
        assertEquals(1,descriptions.size());
        assertEquals("Score = 820 (129.1 bits), Expect = 2.9e-62, Sum P(2) = 2.9e-62\n" +
                " Identities = 438/698 (62%), Positives = 438/698 (62%), Strand = Plus / Plus\n",descriptions.get(0));

    }

    @Test
    public void multiplehspTest() {
        String alignment = "Query:  1334 ACATGTGAAACTTACTCTTAATCACATTTGATGATAATTGCT-GATACTAG 1383\n" +
                "             |||| | ||||| | |||| ||||||||||| ||| |||  | ||| | ||\n" +
                "Sbjct:  3663 ACATATAAAACTCATTCTTGATCACATTTGAAGATGATTTTTTGATCCAAG 3713\n" +
                "\n" +
                " Score = 820 (129.1 bits), Expect = 2.9e-62, Sum P(2) = 2.9e-62\n" +
                " Identities = 438/698 (62%), Positives = 438/698 (62%), Strand = Plus / Plus\n" +
                "\n" +
                "Query:   447 ATCTTCCCAGTCGATTGACACCTGTTATGATTTGCACTTAAA-ATAT-TACCACTTACTA 390\n" +
                "             || |||  |   ||| |||| |    | |  || |  ||| | |||| | | | ||||| \n" +
                "Sbjct:  2758 ATGTTCTGATATGATAGACAACAAGAAAGTATTCCTTTTATACATATATTCTA-TTACTT 2816\n" +
                "\n" +
                " Score = 811 (129.1 bits), Expect = 2.9e-62, Sum P(2) = 2.9e-62\n" +
                " Identities = 438/698 (62%), Positives = 438/698 (62%), Strand = Plus / Minus\n" +
                "\n" +
                "Query:   447 ATCTTCCCAGTCGATTGACACCTGTTATGATTTGCACTTAAA-ATAT-TACCACTTACTA 390\n" +
                "             || |||  |   ||| |||| |    | |  || |  ||| | |||| | | | ||||| \n" +
                "Sbjct:  2758 ATGTTCTGATATGATAGACAACAAGAAAGTATTCCTTTTATACATATATTCTA-TTACTT 2816\n" +
                " ";

        Hit hit = new Hit();
        hit.setAlignment(alignment);

        List<String> descriptions = hit.getDescriptions(alignment) ;
        assertEquals(2,descriptions.size());

        String[] hsps = hit.getHsps(alignment);
        assertEquals(3,hsps.length);
        assertFalse(hit.isReversed(hsps[0])) ;
        assertTrue(hit.isReversed(hsps[1])) ;
        String formattedAlignment = hit.getFormattedAlignment();


        String[] alignments = formattedAlignment.split("<pre");
        assertEquals(6,alignments.length);

        assertEquals("Score = 820 (129.1 bits), Expect = 2.9e-62, Sum P(2) = 2.9e-62\n" +
                " Identities = 438/698 (62%), Positives = 438/698 (62%), Strand = Plus / Plus\n",descriptions.get(0));

        assertEquals("Score = 811 (129.1 bits), Expect = 2.9e-62, Sum P(2) = 2.9e-62\n" +
                " Identities = 438/698 (62%), Positives = 438/698 (62%), Strand = Plus / Minus\n",descriptions.get(1));

    }
}
