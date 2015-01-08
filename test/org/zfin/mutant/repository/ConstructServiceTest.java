package org.zfin.mutant.repository;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.presentation.ConstructSearchFormBean;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class ConstructServiceTest extends AbstractDatabaseTest {

    private ConstructSearchCriteria criteria;

    @Before
    public void initCriteria() {
        criteria = new ConstructSearchCriteria(new ConstructSearchFormBean());
    }

    @Test
    public void getExpressionFigures() {
        List<FigureSummaryDisplay> figureSummaryDisplays = ConstructService.getExpressionSummary("ZDB-GTCONSTRCT-120209-1", criteria);
        assertNotNull(figureSummaryDisplays);
    }




}
