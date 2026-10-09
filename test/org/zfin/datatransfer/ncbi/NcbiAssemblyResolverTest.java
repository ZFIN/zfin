package org.zfin.datatransfer.ncbi;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NcbiAssemblyResolverTest {

    private final NcbiAssemblyResolver resolver = new NcbiAssemblyResolver(Map.of(
            "GRCz12tu", 1L,
            "GRCz13ab", 2L,
            "GRCz12ab", 7L,
            "GRCz11", 3L));

    @Test
    public void resolvesGRCz12tu() {
        assertEquals(Long.valueOf(1L), resolver.resolveAssemblyId("Alternate GRCz12tu"));
    }

    @Test
    public void resolvesGRCz13ab() {
        assertEquals(Long.valueOf(2L), resolver.resolveAssemblyId("Reference GRCz13ab Primary Assembly"));
    }

    @Test
    public void resolvesGRCz12ab() {
        assertEquals(Long.valueOf(7L), resolver.resolveAssemblyId("Reference GRCz12ab Primary Assembly"));
    }

    @Test
    public void returnsNullForBlank() {
        assertNull(resolver.resolveAssemblyId(""));
        assertNull(resolver.resolveAssemblyId(null));
        assertNull(resolver.resolveAssemblyId("-"));
    }

    @Test
    public void returnsNullForUnrecognizedAssembly() {
        assertNull(resolver.resolveAssemblyId("Reference Zv9 Primary Assembly"));
    }

    @Test
    public void matchesWholeWordsOnly() {
        assertNull(resolver.resolveAssemblyId("Reference GRCz11.1 Primary Assembly"));
    }
}
