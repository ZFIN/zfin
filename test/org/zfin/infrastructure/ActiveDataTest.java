package org.zfin.infrastructure;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 */
public class ActiveDataTest {

    @Test
    public void invalidZdbIDNull() {
        ActiveData data = new ActiveData();
        try {
            data.setZdbID(null);
        } catch (RuntimeException re) {
            assertTrue(true);
            assertTrue(re instanceof InvalidZdbID);
            assertTrue(re.getMessage().contains(InvalidZdbID.NULL_MESSSAGE));
            return;
        }
        fail("Null was accepted as a valid zdb active data");
    }

    @Test
    public void invalidZdbIDPrefix() {
        ActiveData data = new ActiveData();
        try {
            data.setZdbID("AAS");
        } catch (RuntimeException re) {
            assertTrue(true);
            assertTrue(re instanceof InvalidZdbID);
            assertTrue(re.getMessage().contains(InvalidZdbID.INCORRECT_PREFIX_MESSSAGE));
            return;
        }
        fail("Null was accepted as a valid zdb active data");
    }

    @Test
    public void invalidZdbIDType() {
        ActiveData data = new ActiveData();
        try {
            data.setZdbID("ZDB-PUB-");
        } catch (RuntimeException re) {
            assertTrue(true);
            assertTrue(re instanceof InvalidZdbID);
            assertTrue(re.getMessage().contains(InvalidZdbID.INCORRECT_TYPE_MESSSAGE));
            return;
        }
        fail("Null was accepted as a valid zdb active data");
    }

    @Test
    public void validZdbIDType() {
        ActiveData data = new ActiveData();
        try {
            data.setZdbID("ZDB-GENE-");
        } catch (RuntimeException re) {
            fail("ZDB-GENE is a valid active data type");
            return;
        }
        assertTrue(true);
    }

}