package org.zfin.database.presentation;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableTest {

    @Test
    public void getZdbEntityTable() {
        Table genox = Table.getEntityTable("ZDB-GENOX-091001-1");
        assertEquals(genox, Table.FISH_EXPERIMENT);
    }

    @Test
    public void getSerialEntityTable() {
        Table genox = Table.getEntityTable("ZDB-FISH-150101-2");
        assertEquals(genox, Table.FISH);
    }

    @Test
    public void getEntityTableByTableName() {
        Table genox = Table.getEntityTableByTableName("fish_experiment");
        assertEquals(genox, Table.FISH_EXPERIMENT);
    }

    @Test
    public void getChildTables() {
        Table genox = Table.FISH_EXPERIMENT;
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

    @Test
    public void hasZdbIdPk() {
        assertTrue(Table.PUBLICATION.hasZdbPk());
        assertTrue(!Table.PHENOTYPE_EXPERIMENT.hasZdbPk());

        List<Table> allTablesWithZdbPk = Table.getAllTablesWithZdbPk();
        assertTrue(allTablesWithZdbPk.size() > 15);
    }
}
