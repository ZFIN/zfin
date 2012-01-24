package org.zfin.database;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.database.presentation.*;
import org.zfin.util.DatabaseJdbcStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class ForeignKeyResultTest extends AbstractDatabaseTest{

    private List<ForeignKeyResult> foreignKeyResults;

    @Test
    public void ForeignKeyResultHierarchySimple() {
        String combinedForeignKey = "genox_geno_zdb_id,xpatex_genox_zdb_id";
        List<ForeignKeyResult> result = DatabaseService.createFKResultList(combinedForeignKey, "ZDB-GENO-070215-11");
        assertNotNull(result);
    }

    @Test
    public void testPublicationRecord() {
        List<ForeignKeyResult> list = DatabaseService.createFKResultList(Table.PUBLICATION, "ZDB-PUB-050309-6");
        assertNotNull(list);
    }


}
