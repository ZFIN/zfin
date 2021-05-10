package org.zfin.gwt;

import org.junit.Test;
import org.zfin.gwt.root.util.StringUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 */
public class GwtStringUtilsTest {


    @Test
    public void isEmpty(){
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));
        assertFalse(StringUtils.isEmpty("  bob  "));
    }

    @Test
    public void isNotEmpty(){
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("bob"));
        assertTrue(StringUtils.isNotEmpty("  bob  "));
    }

    @Test
    public void isEmptyTrim(){
        assertTrue(StringUtils.isEmptyTrim(null));
        assertTrue(StringUtils.isEmptyTrim(""));
        assertTrue(StringUtils.isEmptyTrim(" "));
        assertFalse(StringUtils.isEmptyTrim("bob"));
        assertFalse(StringUtils.isEmptyTrim("  bob  "));
    }

    @Test
    public void isNotEmptyTrim(){
        assertFalse(StringUtils.isNotEmptyTrim(null));
        assertFalse(StringUtils.isNotEmptyTrim(""));
        assertFalse(StringUtils.isNotEmptyTrim(" "));
        assertTrue(StringUtils.isNotEmptyTrim("bob"));
        assertTrue(StringUtils.isNotEmptyTrim("  bob  "));
    }
}
