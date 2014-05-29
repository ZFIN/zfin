package org.zfin.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.springframework.ws.test.support.AssertionErrors.assertTrue;

public class DatabaseJdbcStatementTest {

    @Test
    public void testAssertionStatement() {
        String query = "TEST (fish_annotation_search_records < 16000) 'fish_annotation_search table has fewer than 16,000 records: $x'";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        int value = 1000;
        assertTrue("Ein Test", statement.isTest());
        assertTrue("test is true", statement.isTestTrue(value));
        assertEquals("fish_annotation_search table has fewer than 16,000 records: 1000", statement.getErrorMessage(value));
        assertEquals("fish_annotation_search_records", statement.getDataKey());

    }

    @Test
    public void testKeyStatement() {
        String query = "unload to fish_annotation_search_records \n" +
                "  select count(*) as counter from fish_annotation_search;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);

        assertEquals("fish_annotation_search_records", statement.getDataKey());

    }


}
