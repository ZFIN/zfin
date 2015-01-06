package org.zfin.infrastructure;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 */
public class ActiveSourceTest {

    private ActiveSource source;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initialize() {
        source = new ActiveSource();
    }

    @Test
    public void invalidZdbIDNull() {
        thrown.expect(InvalidZdbID.class);
        thrown.expectMessage(InvalidZdbID.NULL_MESSSAGE);
        source.setZdbID(null);
    }

    @Test
    public void invalidZdbIDPrefix() {
        thrown.expect(InvalidZdbID.class);
        thrown.expectMessage(InvalidZdbID.INCORRECT_PREFIX_MESSSAGE);
        source.setZdbID("AAS");
    }

    @Test
    public void invalidZdbIDType() {
        thrown.expect(InvalidZdbID.class);
        thrown.expectMessage(InvalidZdbID.INCORRECT_TYPE_MESSSAGE);
        source.setZdbID("ZDB-GENE-");
    }

}