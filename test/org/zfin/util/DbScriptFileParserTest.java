package org.zfin.util;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Parse a DB script and create a collection of instructions.
 */
public class DbScriptFileParserTest {

    @Test
    public void parseDatabaseQueryFile() {
        String fileName = "test//dbTestScript.sql";
        File file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");

        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(8, queries.size());
        DatabaseJdbcStatement statement = queries.get(0);
        assertTrue(!statement.isLoadStatement());
        assertEquals("terms_missing_obo_id.txt", statement.getDataKey());
        assertEquals("select * from term   where term_ont_id like 'ZDB-TERM-%' ;", statement.getQuery());

        // second query is a trace statement
        statement = queries.get(1);
        assertEquals("select * from term;", statement.getQuery());

        // insert statement
        statement = queries.get(2);
        assertTrue(statement.isInsertStatement());

        // load statement
        statement = queries.get(5);
        assertTrue(statement.isLoadStatement());
        assertEquals("SELECT * FROM tmp_header;", statement.getDebugStatement().getQuery());

        // delete statement
        statement = queries.get(4);
        assertTrue(statement.isDeleteStatement());
        assertEquals("SELECT * from tmp_zfin_rels   where termrel_term_2_zdb_id is null;", statement.getDebugStatement().getQuery());
        assertEquals("SELECT * FROM tmp_zfin_rels", statement.getDebugDeleteStatement().getQuery());

        // DELETE statement
        statement = queries.get(6);
        assertTrue(statement.isDeleteStatement());
        assertEquals("SELECT * from tmp_zfin_rels   where termrel_term_2_zdb_id is null;", statement.getDebugStatement().getQuery());

        // update statement
        statement = queries.get(7);
        assertTrue(statement.isUpdateStatement());
        assertEquals("SELECT *      tmp_syndef      set scoper = trim(scoper);", statement.getDebugStatement().getQuery());
    }

}
