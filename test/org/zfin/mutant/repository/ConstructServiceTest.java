package org.zfin.mutant.repository;

import org.junit.Before;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.ConstructSearchResult;
import org.zfin.mutant.presentation.Construct;
import org.zfin.mutant.presentation.ConstructSearchFormBean;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

    @Test
    public void getConstructShouldFindByEapTerm() {
        ConstructSearchFormBean form = new ConstructSearchFormBean();
        form.setAnatomyTermIDs("ZDB-TERM-100331-1023,");
        form.setAnatomyTermNames("myotome|");
        form.setAllTypes("allConstructs");
        ConstructSearchCriteria criteria = new ConstructSearchCriteria(form);
        ConstructSearchResult result = ConstructService.getConstruct(criteria);
        assertThat(result, notNullValue());

        List<Construct> results = result.getResults();
        assertThat(results, notNullValue());
        assertThat(results, not(empty()));

        String eapFigId = "ZDB-FIG-140325-68";
        assertThat(getAllExpressionFigureIds(results), hasItem(eapFigId));
    }

    private Collection<String> getAllExpressionFigureIds(List<Construct> constructs) {
        Set<String> ids = new HashSet<>();
        for (Construct construct : constructs) {
            for (ZfinFigureEntity figure : construct.getExpressionFigures()) {
                ids.add(figure.getID());
            }
        }
        return ids;
    }

}
