package org.zfin.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Utilities that are not in StringUtils.
 */
public class ZfinStringUtilsTest {

    @Test
    public void splitStringMethod(){
        String name = "retina,brain";
        String[] array = name.split(",");
        assertEquals(2, array.length);

        name = "retina@@brain";
        array = name.split("@@");
        assertEquals(2, array.length);

        name = "retina|brain";
        array = name.split("\\|");
        assertEquals(2, array.length);
    }
}