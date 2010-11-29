package org.zfin.util;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
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
        assertEquals(6, queries.size());
        DatabaseJdbcStatement statement = queries.get(0);
        assertTrue(!statement.isLoadStatement());
        statement = queries.get(1);
        assertTrue(statement.isLoadStatement());
        assertEquals("syntypedefs_header.unl", statement.getDataKey());
        assertEquals("insert into tmp_syndef;", statement.getQuery());
        statement.updateInsertStatement(3);
        assertEquals("insert into tmp_syndef values (?,?,?);", statement.getQuery());
        assertEquals("tmp_syndef", statement.getTableName());

        statement = queries.get(4);
        assertTrue(statement.isUnloadStatement());
        assertEquals("select * from term   where term_ont_id like 'ZDB-TERM-%' ;", statement.getQuery());

        statement = queries.get(5);
        assertTrue(statement.isEcho());
        assertEquals("\"update the term table with new names where the term id is the same term id in the obo file\" ;", statement.getQuery());

    }

}
