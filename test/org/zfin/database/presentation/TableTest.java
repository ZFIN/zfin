package org.zfin.database.presentation;

import org.junit.Test;
import org.zfin.database.DatabaseService;
import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableTest {

    @Test
    public void getZdbEntityTable() {
        Table genox = Table.getEntityTable("ZDB-GENOX-091001-1");
        assertEquals(genox, Table.GENOTYPE_EXPERIMENT);
    }

    @Test
    public void getSerialEntityTable() {
        Table genox = Table.getEntityTable("FISH-22145");
        assertEquals(genox, Table.WH_FISH);
    }

    @Test
    public void getEntityTableByTableName() {
        Table genox = Table.getEntityTableByTableName("genotype_experiment");
        assertEquals(genox, Table.GENOTYPE_EXPERIMENT);
    }

    @Test
    public void getChildTables() {
        Table genox = Table.GENOTYPE_EXPERIMENT;
        List<Table> childTables = genox.getChildTables();
        assertNotNull(childTables);
        assertTrue(childTables.size() >= 4);

        genox = Table.PHENOTYPE_EXPERIMENT;
        childTables = genox.getChildTables();
        assertNotNull(childTables);
        assertTrue(childTables.size() >= 1);
    }

    @Test
    public void validateTableDefinitions() {
        Table.validateUniquePkIdentifier();
    }
}
