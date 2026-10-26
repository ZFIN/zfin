package org.zfin.datatransfer.go.service;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ZFIN-10570 / ZFIN-10569: annotation-extension relations arrive by ID in GPAD (RO:, BFO:,
 * GOREL:) and by name in the legacy GAF. An ID must be looked up by ID; anything else by name.
 * GOREL IDs used to fall through to the name lookup, which can never match them.
 */
public class GafRelationIdTest {

    @Test
    public void roBfoAndGorelIdsAreIds() {
        assertTrue(GafService.isRelationId("RO:0002349"));
        assertTrue(GafService.isRelationId("BFO:0000066"));
        assertTrue(GafService.isRelationId("GOREL:0001019"));
        assertTrue(GafService.isRelationId(" RO:0002233 "));
    }

    @Test
    public void relationNamesAreNotIds() {
        assertFalse(GafService.isRelationId("results_in_division_of"));
        assertFalse(GafService.isRelationId("part_of"));
        assertFalse(GafService.isRelationId("acts_upstream_of_or_within,_positive_effect"));
        assertFalse(GafService.isRelationId(""));
        assertFalse(GafService.isRelationId(null));
    }
}
