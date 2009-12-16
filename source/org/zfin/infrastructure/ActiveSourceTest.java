package org.zfin.infrastructure;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 */
public class ActiveSourceTest {

    @Test
    public void invalidZdbIDNull() {
        ActiveSource source = new ActiveSource();
        try {
            source.setZdbID(null);
        } catch (RuntimeException re) {
            assertTrue(true);
            assertTrue(re instanceof InvalidZdbID);
            assertTrue(re.getMessage().contains(InvalidZdbID.NULL_MESSSAGE));
            return;
        }
        fail("Null was accepted as a valid zdb active source");
    }

    @Test
    public void invalidZdbIDPrefix() {
        ActiveSource source = new ActiveSource();
        try {
            source.setZdbID("AAS");
        } catch (RuntimeException re) {
            assertTrue(true);
            assertTrue(re instanceof InvalidZdbID);
            assertTrue(re.getMessage().contains(InvalidZdbID.INCORRECT_PREFIX_MESSSAGE));
            return;
        }
        fail("Null was accepted as a valid zdb active source");
    }

    @Test
    public void invalidZdbIDType() {
        ActiveSource source = new ActiveSource();
        try {
            source.setZdbID("ZDB-GENE-");
        } catch (RuntimeException re) {
            assertTrue(true);
            assertTrue(re instanceof InvalidZdbID);
            assertTrue(re.getMessage().contains(InvalidZdbID.INCORRECT_TYPE_MESSSAGE));
            return;
        }
        fail("Null was accepted as a valid zdb active source");
    }

}