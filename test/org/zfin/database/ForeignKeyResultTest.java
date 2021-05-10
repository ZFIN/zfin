package org.zfin.database;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.database.presentation.ForeignKeyResult;
import org.zfin.database.presentation.Table;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class ForeignKeyResultTest extends AbstractDatabaseTest{

    @Test
    public void ForeignKeyResultHierarchySimple() {
        String combinedForeignKey = "genofeat_geno_zdb_id,genofeat_feature_zdb_id";
        List<ForeignKeyResult> result = DatabaseService.createFKResultList(combinedForeignKey, "ZDB-GENO-070215-11");
        assertNotNull(result);
    }

    @Test
    public void testPublicationRecord() {
        List<ForeignKeyResult> list = DatabaseService.createFKResultList(Table.PUBLICATION, "ZDB-PUB-050309-6");
        assertNotNull(list);
    }


}
