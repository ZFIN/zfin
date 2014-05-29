package org.zfin.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ByteUtilTest {


    @Test
    public void getBytesWithUnit() {
        long bytes = 59;
        String bytesWithUnit = ByteUtil.getBytesWithUnit(bytes);
        assertNotNull(bytesWithUnit);
        assertEquals("59 bytes", bytesWithUnit);

        bytes = 1064;
        bytesWithUnit = ByteUtil.getBytesWithUnit(bytes);
        assertNotNull(bytesWithUnit);
        assertEquals("1 KB", bytesWithUnit);


    }


}
