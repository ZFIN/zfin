package org.zfin.gwt;

import org.junit.Test;
import org.zfin.gwt.root.dto.GoPubEnum;
import org.zfin.gwt.root.dto.InferenceCategory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class InferenceCategoryUnitTest {

    @Test
    public void simpleMatch(){
        String testString1 = "GenBank:12345" ;
        assertTrue(testString1.matches("GenBank:.*")) ;
        String testString2 = "ZFIN:ZDB-MRPHLNO-12345" ;
        assertTrue(testString2.matches("ZFIN:ZDB-MRPHLNO-.*|asdf")) ;
        assertEquals("GenBank:.*",InferenceCategory.GENBANK.match());
        assertEquals("ZFIN:ZDB-MRPHLNO-.*|ZFIN:ZDB-GENO-.*",InferenceCategory.ZFIN_MRPH_GENO.match());
        assertEquals("ZFIN:ZDB-GENE-.*",InferenceCategory.ZFIN_GENE.match());
//        assertEquals("GenBank:.*",InferenceCategory.GENBANK.match());
//        "GenBank.*".
        assertEquals(InferenceCategory.GENBANK,InferenceCategory.getInferenceCategoryByValue(testString1)) ;
        assertEquals(InferenceCategory.ZFIN_MRPH_GENO,InferenceCategory.getInferenceCategoryByValue(testString2)) ;
        assertEquals(InferenceCategory.ZFIN_GENE,InferenceCategory.getInferenceCategoryByValue("ZFIN:ZDB-GENE-123213")) ;
        assertEquals(InferenceCategory.ZFIN_MRPH_GENO,InferenceCategory.getInferenceCategoryByValue("ZFIN:ZDB-GENO-1234")) ;
    }
}
