package org.zfin.database.presentation;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ForeignKeyTest {

    @Test
    public void getForeignKeys() {
        Table genox = Table.GENOTYPE_EXPERIMENT;
        List<ForeignKey> foreignKeyList = ForeignKey.getForeignKeys(genox);
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() >= 4);
    }

    @Test
    public void getForeignKeyByColumnName() {
        ForeignKey foreignKey = ForeignKey.getForeignKeyByColumnName("phenox_genox_zdb_id");
        assertNotNull(foreignKey);
        assertTrue(foreignKey == ForeignKey.PHENOX_GENOX);
    }

    @Test
    public void getForeignKeysByLevel() {
        Table genox = Table.GENOTYPE_EXPERIMENT;
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
        Table table = ForeignKey.getRootTableFromNodeName(fullNode);
        assertNotNull(table);
        assertEquals(Table.FEATURE, table);
    }

    //@Test
    public void foreignKeyMap() {

        ForeignKey.createDagMap(Table.ONTOLOGY);
        Map map = ForeignKey.dagMap;
        assertNotNull(map);
    }

    //@Test
    public void getJoinedForeignKeys() {
        Table genox = Table.GENOTYPE_EXPERIMENT;
        List<ForeignKey> foreignKeyList = ForeignKey.getJoinedForeignKeys("phenos_phenox_pk_id", genox.getTableName());
        assertNotNull(foreignKeyList);
        assertTrue(foreignKeyList.size() >= 4);
    }
}
