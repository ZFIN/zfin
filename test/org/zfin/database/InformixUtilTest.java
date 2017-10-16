package org.zfin.database;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.properties.ZfinPropertiesEnum;

public class InformixUtilTest extends AbstractDatabaseTest {

    @Test
    public void testFunctionCall() {
        if (ZfinPropertiesEnum.USE_POSTGRES.value().equals("true"))
            InformixUtil.runInformixProcedure("regen_construct_marker", "GENE");
    }
}
