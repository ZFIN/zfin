package org.zfin.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.springframework.ws.test.support.AssertionErrors.assertTrue;

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


}
