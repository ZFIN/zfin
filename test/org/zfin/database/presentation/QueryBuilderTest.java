package org.zfin.database.presentation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.zfin.util.SqlQueryKeywords.EQUALS;
import static org.zfin.util.SqlQueryKeywords.TABLE;

/**
 *
 */
public class QueryBuilderTest {

    @Test
    public void testSimpleCount() {
        QueryBuilder builder = new QueryBuilder(null);
        builder.addWhereClause("zdb_id", "walter");
        builder.addTable(Table.PUBLICATION.getTableName());

        assertEquals("SELECT COUNT(*) FROM publication WHERE zdb_id = 'walter'", builder.getSqlQuery());
    }

    @Test
    public void testSimpleSelect() {
        QueryBuilder builder = new QueryBuilder(Table.PUBLICATION);
        builder.addWhereClause("zdb_id", "walter");
        builder.addTable(Table.PUBLICATION.getTableName());

        assertEquals("SELECT * FROM publication WHERE zdb_id = 'walter'", builder.getSqlQuery());
    }

    @Test
    public void testSimpleSelectSingleColumn() {
        QueryBuilder builder = new QueryBuilder(Table.PUBLICATION);
        builder.addSelectColumn("authors");
        builder.addWhereClause("zdb_id", "walter");
        builder.addTable(Table.PUBLICATION.getTableName());

        assertEquals("SELECT authors FROM publication WHERE zdb_id = 'walter'", builder.getSqlQuery());
    }
}
