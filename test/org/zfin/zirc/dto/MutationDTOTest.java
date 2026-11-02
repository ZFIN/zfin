package org.zfin.zirc.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * ZFIN-10543: the General section shows an existing ZFIN allele's lab of origin and
 * institution in place of the free-text institution.
 */
public class MutationDTOTest {

    @Test
    public void labAndInstitution() {
        assertEquals("Westerfield Lab, Oregon", MutationDTO.labAndInstitution("Westerfield Lab", "Oregon"));
    }

    @Test
    public void onlyOneOfThem() {
        assertEquals("Westerfield Lab", MutationDTO.labAndInstitution("Westerfield Lab", null));
        assertEquals("Oregon", MutationDTO.labAndInstitution(" ", "Oregon"));
    }

    @Test
    public void neither() {
        assertNull(MutationDTO.labAndInstitution(null, ""));
    }
}
