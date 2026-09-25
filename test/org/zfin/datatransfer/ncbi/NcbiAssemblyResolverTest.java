package org.zfin.datatransfer.ncbi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NcbiAssemblyResolverTest {

    @Test
    public void resolvesGRCz12tu() {
        assertEquals(Long.valueOf(NcbiAssemblyResolver.GRCZ12TU_ASSEMBLY_ID),
                NcbiAssemblyResolver.resolveAssemblyId("Alternate GRCz12tu"));
    }

    @Test
    public void resolvesGRCz13ab() {
        assertEquals(Long.valueOf(NcbiAssemblyResolver.GRCZ13AB_ASSEMBLY_ID),
                NcbiAssemblyResolver.resolveAssemblyId("Reference GRCz13ab Primary Assembly"));
    }

    @Test
    public void returnsNullForBlank() {
        assertNull(NcbiAssemblyResolver.resolveAssemblyId(""));
        assertNull(NcbiAssemblyResolver.resolveAssemblyId(null));
        assertNull(NcbiAssemblyResolver.resolveAssemblyId("-"));
    }

    @Test
    public void returnsNullForUnrecognizedAssembly() {
        assertNull(NcbiAssemblyResolver.resolveAssemblyId("Reference Zv9 Primary Assembly"));
    }
}
