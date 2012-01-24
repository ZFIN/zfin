package org.zfin.database.presentation;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.database.DatabaseService;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class DatabaseServiceDbTest extends AbstractDatabaseTest {

    @Test
    public void getPublicationInfo() {
        Table rootTable = Table.PUBLICATION;
        String id = "ZDB-PUB-010705-7";
        List<ForeignKeyResult> resultList = DatabaseService.createFKResultList(rootTable, id);
        assertNotNull(resultList);
    }

    @Test
    public void getGenoxInfoFromGenoID() {
        String foreignKeyName = "fas_genotype_group";
        String parentPkValue = "ZDB-GENO-070215-11";
        List<ForeignKeyResult> resultList = DatabaseService.createFKResultList(foreignKeyName, parentPkValue);
        assertNotNull(resultList);
    }

    @Test
    public void getExpressionExperimentInfoFromGenoXID() {
        String parentPkValue = "ZDB-GENOX-041102-2604";
        String entityName = DatabaseService.getEntityName(parentPkValue, Table.GENOTYPE_EXPERIMENT);
        assertNotNull(entityName);
        assertEquals("wild type (unspecified) || _Standard", entityName);
    }

    @Test
    public void getGenoInfoFromGenoID() {
        String parentPkValue = "ZDB-GENO-030619-2";
        String entityName = DatabaseService.getEntityName(parentPkValue, Table.GENOTYPE);
        assertNotNull(entityName);
        assertEquals("wild type (unspecified)", entityName);
    }

    @Test
    public void getExpressionExperimentInfoFromGenoID() {
        String parentPkValue = "ZDB-GENO-070215-11";
        DatabaseQueryFormBean formBean = new DatabaseQueryFormBean();
        formBean.setForeignKeyName("genox_geno_zdb_id|xpatex_genox_zdb_id");
        formBean.setParentPkValue(parentPkValue);
        List<ForeignKeyResult> resultList = DatabaseService.createFKResultList(formBean.getForeignKeyName(), formBean.getParentPkValue());
        assertNotNull(resultList);
    }

}
