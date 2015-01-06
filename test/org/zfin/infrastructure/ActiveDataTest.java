package org.zfin.infrastructure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

/**
 */
public class ActiveDataTest {

    private ActiveData data;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initialize() {
        data = new ActiveData();
    }

    @Test
    public void invalidZdbIDNull() {
        thrown.expect(InvalidZdbID.class);
        thrown.expectMessage(InvalidZdbID.NULL_MESSSAGE);
        data.setZdbID(null);
    }

    @Test
    public void invalidZdbIDPrefix() {
        thrown.expect(InvalidZdbID.class);
        thrown.expectMessage(InvalidZdbID.INCORRECT_PREFIX_MESSSAGE);
        data.setZdbID("AAS");
    }

    @Test
    public void invalidZdbIDType() {
        thrown.expect(InvalidZdbID.class);
        thrown.expectMessage(InvalidZdbID.INCORRECT_TYPE_MESSSAGE);
        data.setZdbID("ZDB-PUB-");
    }

    @Test
    public void validZdbIDType() {
        data.setZdbID("ZDB-GENE-");
    }

    @Test
    public void getTypeForId() {
        String zdbID = "ZDB-TALEN-120304-11";
        boolean isTalen = ActiveData.isValidActiveData(zdbID, ActiveData.Type.TALEN);
        assertTrue(zdbID + " should be active talen data", isTalen);
    }

}