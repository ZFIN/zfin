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
        assertEquals(3, queries.size());
        assertEquals("insert into gff3 ;", queries.get(1).getQuery());
        assertEquals("select * from tmp_vega_thisse_report order by 1,2  ;", queries.get(2).getQuery());
    }

    @Test
    // This checks if the parser gets the right number of queries from select files.
    // This is a bit volatile as any change in the number of queries in those files
    // will break this unit test. But it's worth it for now as we adjust the parser...
    public void checkSqlFiles() {
        String fileName = "server_apps/data_transfer/Downloads/GFF3/download-files/E_zfin_ensembl_gene_PG.sql";
        File file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(57, queries.size());

        fileName = "server_apps/data_transfer/LoadOntology/handleRelationships_PG.sql";
        file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        parser = new DbScriptFileParser(file);
        queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(33, queries.size());

        fileName = "server_apps/data_transfer/LoadOntology/handleSynonyms_PG.sql";
        file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        parser = new DbScriptFileParser(file);
        queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(41, queries.size());

        fileName = "server_apps/data_transfer/LoadOntology/loadTerms_PG.sql";
        file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        parser = new DbScriptFileParser(file);
        queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(87, queries.size());

        fileName = "server_apps/data_transfer/LoadOntology/loadDBxrefs_PG.sql";
        file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        parser = new DbScriptFileParser(file);
        queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(12, queries.size());

        fileName = "server_apps/data_transfer/LoadOntology/loadSubsets_PG.sql";
        file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");
        parser = new DbScriptFileParser(file);
        queries = parser.parseFile();
        assertNotNull(queries);
        assertEquals(28, queries.size());
    }


}
