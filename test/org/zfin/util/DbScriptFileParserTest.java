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
    public void parsePostgresCopyFile() {
        String fileName = "test//postgresCopyCommand.sql";
        File file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(1, queries.size());
        assertEquals("\t", queries.get(0).getDelimiter());
        assertEquals("select gff_seqname,         'ZFIN' gff_source,         gff_feature,         gff_start,         gff_end,         gff_score,         gff_strand,         gff_frame,         'ID=' || feature_zdb_id         ||';Name=' || feature_abbrev         ||';Alias='|| feature_zdb_id || ','         || feature_abbrev || ','         || feature_name   || ';' as attribute       from  gff3 join feature on substring(feature_abbrev from 1 for 8) = gff_id       where gff_source = 'BurgessLin'             and gff_feature = 'Transgenic_insertion' order by 1,4,5,9", queries.get(0).getQuery());
    }

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
        assertTrue(!statement.isLoadStatement());

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

    @Test
    public void parseLoadFileData() {
        String fileName = "test//postgresLoadCommand.sql";
        File file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(1, queries.size());
        assertEquals("insert into gff3", queries.get(0).getQuery());
    }


}
