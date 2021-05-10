package org.zfin.database;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;

public class InformixUtilTest extends AbstractDatabaseTest {

    @Test
    public void testFunctionCall() {
        InformixUtil.runProcedure("regen_construct_marker", "GENE");
    }
}
