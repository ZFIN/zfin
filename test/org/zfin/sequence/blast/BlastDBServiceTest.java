package org.zfin.sequence.blast;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.sequence.service.UniprotService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 */
public class BlastDBServiceTest extends AbstractDatabaseTest{

    private UniprotService uniprotService = new UniprotService();

    @Test
    public void useLocalForUniprot(){
        assertFalse(uniprotService.validateAccession("NP_571379")) ;
        assertTrue(uniprotService.validateAccession("B3DJJ0")) ;
    }

    @Test
    public void validateUrlOnly(){
        assertTrue(uniprotService.validateAgainstUniprotWebsite("B3DJJ0")) ;
        assertFalse(uniprotService.validateAgainstUniprotWebsite("NP_571379"));
    }
}
