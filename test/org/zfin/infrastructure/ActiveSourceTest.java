package org.zfin.infrastructure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ActiveSourceTest {

    private static ActiveSource source;

    @BeforeAll
    public static void initialize() {
        source = new ActiveSource();
    }

    @Test
    public void invalidZdbIDNull() {
        Assertions.assertThrows(InvalidZdbID.class, () -> source.setZdbID(null));
    }

    @Test
    public void invalidZdbIDPrefix() {
        Assertions.assertThrows(InvalidZdbID.class, () -> source.setZdbID("AAS"));
    }

    @Test
    public void invalidZdbIDType() {
        Assertions.assertThrows(InvalidZdbID.class, () -> source.setZdbID("ZDB-GENE"));
    }

}