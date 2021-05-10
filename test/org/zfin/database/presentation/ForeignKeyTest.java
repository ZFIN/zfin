package org.zfin.database.presentation;

import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.database.DatabaseService;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ForeignKeyTest extends AbstractDatabaseTest {

    @Test
    public void getForeignKeys() {
        Table genox = Table.FISH_EXPERIMENT;
        List<ForeignKey> foreignKeyList = ForeignKey.getForeignKeys(genox);
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() >= 4);
    }

    @Test
    public void getForeignKeyByColumnName() {
        ForeignKey foreignKey = ForeignKey.getForeignKeyByColumnName("phenox_genox_zdb_id");
        assertNotNull(foreignKey);
        assertTrue(foreignKey == ForeignKey.PHENOX_FISHOX);
    }

    @Test
    public void getForeignKeysByLevel() {
        Table genox = Table.FISH_EXPERIMENT;
        List<ForeignKey> foreignKeyList = ForeignKey.getForeignKeys(genox, 1);
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() >= 4);

        foreignKeyList = ForeignKey.getForeignKeys(genox, 2);
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() >= 2);
    }

    @Test
    public void getForeignKeysFromManyToManyRelationship() {
        String fkHierarchy = "termsub_term_zdb_id,termsub_subset_id";
        List<ForeignKey> foreignKeyList = ForeignKey.getForeignKeyHierarchy(fkHierarchy);
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() == 1);
        assertEquals(ForeignKey.TERM_SUBSET_ASSOC, foreignKeyList.get(0));

        fkHierarchy = "termsub_subset_id,termsub_term_zdb_id";
        foreignKeyList = ForeignKey.getForeignKeyHierarchy(fkHierarchy);
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() == 1);
        assertEquals(ForeignKey.SUBSET_TERM_ASSOC, foreignKeyList.get(0));
    }

    @Test
    public void getForeignKeysFromDataSupplier() {
        String fkHierarchy = "genotype:idsup_data_zdb_id,lab:idsup_supplier_zdb_id";
        List<ForeignKey> foreignKeyList = ForeignKey.getForeignKeyHierarchy(fkHierarchy);
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() == 1);
        assertEquals(ForeignKey.GENO_LAB_ASSOC, foreignKeyList.get(0));
    }

    @Test
    public void getRootTableFromNodeName() {
        String fullNode = "feature:dalias_data_zdb_id";
        Table table = ForeignKey.getRootTableFromNodeName(fullNode, null);
        assertNotNull(table);
        assertEquals(Table.FEATURE, table);
    }

    @Test
    public void getRootTableFromNodeNameAntibody() {
        String fullNode = "extnote_data_zdb_id";
        Table table = ForeignKey.getRootTableFromNodeName(fullNode, Table.ANTIBODY);
        assertNotNull(table);
        assertEquals(Table.ANTIBODY, table);
    }

    @Test
    @Ignore("broken")
    public void foreignKeyMap() {

        ForeignKey.createDagMap(Table.ONTOLOGY);
        Map map = ForeignKey.dagMap;
        assertNotNull(map);
    }

    @Test
    public void ExpressionResult() {

        List<ForeignKeyResult> foreignKeyResultList = DatabaseService.createFKResultList(Table.FISH, "ZDB-FISH-150721-100");
        ForeignKey.createDagMap(Table.ONTOLOGY);
        Map map = ForeignKey.dagMap;
        assertNotNull(map);
    }

    @Test
    @Ignore("broken")
    public void getJoinedForeignKeys() {
        Table genox = Table.FISH_EXPERIMENT;
        List<ForeignKey> foreignKeyList = ForeignKey.getJoinedForeignKeys("phenos_phenox_pk_id", genox.getTableName());
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() >= 4);
    }
}
