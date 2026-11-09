package org.zfin.mapping;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.genomebrowser.GenomeBrowserBuild;

import java.util.Map;

import static org.junit.Assert.*;

public class AssemblyOrderTest extends AbstractDatabaseTest {

    @Test
    public void ranksFollowTheAssemblyTableNewestFirst() {
        AssemblyOrder order = AssemblyOrder.fromDatabase();
        assertTrue(order.isNewer("GRCz13ab", "GRCz12tu"));
        assertTrue(order.isNewer("GRCz12tu", "GRCz11"));
        assertTrue(order.isNewer("GRCz11", "Zv9"));
        assertFalse(order.isNewer("Zv9", "GRCz12tu"));
        assertFalse(order.isNewer("GRCz11", "GRCz11"));
    }

    @Test
    public void anAssemblyTheTableDoesNotListRanksLast() {
        AssemblyOrder order = AssemblyOrder.fromDatabase();
        assertTrue(order.isNewer("Zv9", "no-such-assembly"));
        assertEquals(Integer.MAX_VALUE, order.rank("no-such-assembly"));
        assertEquals(Integer.MAX_VALUE, new AssemblyOrder(Map.of()).rank("GRCz12tu"));
    }

    @Test
    public void aBrowserBuildIsFoundByTheNameOfItsAssembly() {
        assertEquals(GenomeBrowserBuild.CURRENT, GenomeBrowserBuild.forAssembly("GRCz12tu").orElseThrow());
        assertEquals(GenomeBrowserBuild.GRCZ11, GenomeBrowserBuild.forAssembly("GRCz11").orElseThrow());
        assertEquals(GenomeBrowserBuild.ZV9, GenomeBrowserBuild.forAssembly("Zv9").orElseThrow());
        assertTrue(GenomeBrowserBuild.forAssembly("GRCz13ab").isEmpty());
        assertTrue(GenomeBrowserBuild.forAssembly(null).isEmpty());
    }
}
