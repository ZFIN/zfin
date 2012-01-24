package org.zfin.framework.search;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class SearchCriterionTest {

    @Test
    public void testNameSeparator() {
        SearchCriterion criterion = new SearchCriterion(SearchCriterionType.PHENOTYPE_ANATOMY_ID, true);
        criterion.setValue("ZDB-TERM-091022-1,ZDB-TERM-091022-2");
        criterion.setName("heart|brain");
        criterion.setSeparator(",");
        criterion.setNameSeparator("\\|");
        assertEquals(2, criterion.getValues().size());
        assertEquals(2, criterion.getNames().size());
    }
}


