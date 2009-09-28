package org.zfin.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.zfin.framework.presentation.tags.DeflineTag;
import org.zfin.marker.presentation.client.SequenceValidator;
import org.apache.log4j.Logger;

public class SequenceTest {

    private Logger logger = Logger.getLogger(SequenceTest.class) ;

    private String testString1 = "lcl|ZFINPROT0000000077|ZDB-TSCRIPT-081112-10003 publishedProtein ottdart00000025400 transcript   13 aa" ;
    private String testString2 = "tpe|OTTDART00000026759|OTTDARG00000021143|ZDB-GENE-040718-349 sirt5|sirt5 LG_20 BX511260.11 Danio rerio protein_coding KNOWN, 1948 bp" ;
    private String testString3 = "lcl|ENSDART00000066225|gene:ENSDARG00000045045 cdna:known chromosome:ZFISH7:25:15179762:15200474:1 " ;

    @Test
    public void replaceMarkerLink(){
        DeflineTag deflineTag = new DeflineTag() ;
        int index1 = testString1.indexOf("ZDB-TSCRIPT-081112-10003")  ;
        String outputDefline = deflineTag.replaceMarkerWithLink(new String(testString1)) ;
        logger.debug("output defline: " + outputDefline);

        int index2 =
                outputDefline.indexOf(
                        "<a href=/action/marker/transcript-view?zdbID=ZDB-TSCRIPT-081112-10003>ZDB-TSCRIPT-081112-10003</a>") ;
        assertEquals("indexes should be equal",index1,index2) ;
    }


    @Test
    public void replaceEnsdargLink(){
        DeflineTag deflineTag = new DeflineTag() ;
        int index1 = testString3.indexOf("gene:ENSDARG00000045045")  ;
        String outputDefline = deflineTag.replaceEndargsWithLink(new String(testString3)) ;
        logger.debug("output defline: " + outputDefline);

        int index2 =
                outputDefline.indexOf(
                        "<a href=http://www.ensembl.org/Danio_rerio/geneview?gene=ENSDARG00000045045>gene:ENSDARG00000045045</a>"
                        ) ;
        assertEquals("indexes should be equal",index1,index2) ;
    }


    @Test
    public void validateNucletodieSequence(){
        assertEquals(0,
                SequenceValidator.validateNucleotideSequence("abcderfg")) ;
        assertEquals(SequenceValidator.NOT_FOUND,
                SequenceValidator.validateNucleotideSequence("ATCGUMKRYVBHDWSN-")) ;
        assertEquals(SequenceValidator.NOT_FOUND,
                SequenceValidator.validateNucleotideSequence("AAAAAATTTTGGGGGCCCCUUU")) ;
        assertEquals(5,
                SequenceValidator.validateNucleotideSequence("ATCGUZMKRYVBHDWSN-")) ;
    }

    @Test
    public void validatePolypeptideSequence(){
        assertEquals(0,
                SequenceValidator.validatePolypeptideSequence("abcderfg")) ;
        assertEquals(SequenceValidator.NOT_FOUND,
                SequenceValidator.validatePolypeptideSequence("LSEAVGKTRPDIQNFYHMCW*-")) ;
        assertEquals(19,
                SequenceValidator.validatePolypeptideSequence("AAAAAATTTTGGGGGCCCCUUU")) ;
        assertEquals(4,
                SequenceValidator.validatePolypeptideSequence("ATCGUZMKRYVBHDWSN-")) ;
    }
}
