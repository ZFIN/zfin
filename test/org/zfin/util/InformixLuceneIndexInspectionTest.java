package org.zfin.util;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.database.presentation.Table;

import static junit.framework.Assert.assertNotNull;

/**
 * Inspect Informix Lucene index. Find out how many max clauses are needed.
 */
public class InformixLuceneIndexInspectionTest extends AbstractDatabaseTest{

    @Test
    public void findTokensMap(){
        String columnName = "fas_all";
        InformixLuceneIndexInspection lucene = new InformixLuceneIndexInspection(Table.WH_FISH, columnName);
        String queryString = lucene.getMaxTokenQueryString();
        int maxClauseCount = lucene.getMaxTokenQueryStringCount();
        assertNotNull(queryString);
    }
}
