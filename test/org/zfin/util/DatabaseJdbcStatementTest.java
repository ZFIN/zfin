package org.zfin.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseJdbcStatementTest {



    @Test
    public void testSelectIntoStatement() {
        String query = "SELECT   rec_id," +
                "         old_value," +
                "         new_value," +
                "         when" +
                "FROM     updates" +
                "WHERE    when > today -31" +
                "AND      Get_obj_type(rec_id) IN ('GENE'," +
                "                                  'GENEP')" +
                "AND      field_name ='mrkr_abbrev'" +
                "ORDER BY when DESC" +
                "INTO temp gene_names_changed_temp;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        assertTrue("Query is not a read-only statement", !statement.isReadOnlyStatement());

    }

    @Test
    public void debugStatementStripsOnConflictFromInsert() {
        String query = "insert into all_term_contains (a, b, c) " +
                "select x, y, 0 from tmp_term " +
                "on conflict (a, b) do nothing;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        String debugQuery = statement.getDebugStatement().getQuery();
        assertFalse("debug SELECT must not contain ON CONFLICT: " + debugQuery,
                debugQuery.toLowerCase().contains("on conflict"));
        assertTrue("debug SELECT should retain the SELECT body: " + debugQuery,
                debugQuery.toLowerCase().contains("select x, y, 0 from tmp_term"));
    }

    @Test
    public void debugStatementStripsOnConflictCaseInsensitive() {
        String query = "INSERT INTO foo (a, b) SELECT x, y FROM src ON CONFLICT (a) DO NOTHING;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        String debugQuery = statement.getDebugStatement().getQuery();
        assertFalse("debug SELECT must not contain ON CONFLICT: " + debugQuery,
                debugQuery.toLowerCase().contains("on conflict"));
    }

    @Test
    public void debugStatementStripsOnConflictAcrossNewlines() {
        String query = "insert into foo (a, b) select x, y from src\n" +
                "  on conflict (a)\n" +
                "  do nothing;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        String debugQuery = statement.getDebugStatement().getQuery();
        assertFalse("debug SELECT must not contain ON CONFLICT: " + debugQuery,
                debugQuery.toLowerCase().contains("on conflict"));
    }

    @Test
    public void debugStatementStripsReturningFromInsert() {
        String query = "insert into foo (a, b) select x, y from src returning a;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        String debugQuery = statement.getDebugStatement().getQuery();
        assertFalse("debug SELECT must not contain RETURNING: " + debugQuery,
                debugQuery.toLowerCase().contains("returning"));
    }

    @Test
    public void debugStatementPreservesPlainInsertSelect() {
        String query = "insert into foo (a, b) select x, y from src;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        assertEquals("select x, y from src;", statement.getDebugStatement().getQuery());
    }

}
