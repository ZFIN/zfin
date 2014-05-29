package org.zfin.marker.presentation;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbLinkDisplayComparatorTest {

    @Test
    public void testIntegerComparison() {
        assertEquals(-1, new Integer(1).compareTo(2));
        assertEquals(1, new Integer(1).compareTo(0));
        assertEquals(0, new Integer(1).compareTo(1));
        assertEquals(1, ObjectUtils.compare(1, null));
        assertEquals(-1, ObjectUtils.compare(null, 1));
        assertEquals(0, ObjectUtils.compare(null, null));
    }
}
