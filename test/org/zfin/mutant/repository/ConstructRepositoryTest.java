package org.zfin.mutant.repository;

import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.ExpressionResult;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.WarehouseSummary;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.SortBy;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.ConstructSearchResult;
import org.zfin.mutant.presentation.Construct;
import org.zfin.mutant.presentation.ConstructSearchFormBean;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getConstructRepository;

public class ConstructRepositoryTest extends AbstractDatabaseTest {


    @Test
    public void getExpressionFigures() {
        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getConstructRepository().getAllFigures("ZDB-TGCONSTRCT-090702-2");
        assertNotNull(zfinFigureEntities);
    }

    @Test
    public void getExpressionFiguresForSingleTerm() {
        // brain
        String termID = "ZDB-TERM-100331-8";
        // WT (unspecified) + MO1-acd
        String constructID = "ZDB-GTCONSTRCT-120209-1";
        List<String> termList = new ArrayList<String>(1);
        termList.add(termID);

        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getConstructRepository().getFiguresByConstructAndTerms(constructID, termList);
        assertNotNull(zfinFigureEntities);
    }

    @Test
    public void getExpressionFiguresForMultipleTerms() {
        // brain nucleus
        String brainNucleus = "ZDB-TERM-100331-8";
        // eye
        String eye = "ZDB-TERM-100331-100";
        // gli2aty17a/ty17a
        String constructID = "ZDB-TGCONSTRCT-070117-7";
        List<String> termList = new ArrayList<String>(2);
        termList.add(brainNucleus);
        termList.add(eye);

        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getConstructRepository().getFiguresByConstructAndTerms(constructID, termList);
        assertNotNull(zfinFigureEntities);
        assertTrue(zfinFigureEntities.size() >= 2);
    }

    @Test
    public void getConstruct() {


        String constructID = "ZDB-TGCONSTRCT-070117-7";
        Construct construct=RepositoryFactory.getConstructRepository().getConstruct(constructID);
        assertNotNull(construct);

    }
    @Test
    public void getExpressionForConstructs() {
        // brain nucleus
        String brainNucleus = "ZDB-TERM-100331-8";
        // eye
        String eye = "ZDB-TERM-100331-100";
        // gli2aty17a/ty17a
        String constructID = "ZDB-TGCONSTRCT-070117-7";
        List<String> termList = new ArrayList<String>(2);
        termList.add(brainNucleus);
        termList.add(eye);
        List<ExpressionResult> expResults= RepositoryFactory.getConstructRepository().getExpressionForConstructs(constructID, termList);

        assertNotNull(expResults);
        assertTrue(expResults.size() >= 2);
    }

    @Test
    public void termTest() {
        ConstructSearchFormBean formBean = new ConstructSearchFormBean();
        formBean.setAnatomyTermIDs("ZDB-TERM-100331-449"); //pre-optic area, a small number of results
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);

        ConstructSearchCriteria criteria = new ConstructSearchCriteria(formBean);

        ConstructSearchResult result = RepositoryFactory.getConstructRepository().getConstructs(criteria);
        assertTrue("'pre-optic area' term search returns results", result != null && result.getResults() != null && result.getResultsFound() > 0);
    }

    @Test
    public void constructTest() {
        ConstructSearchFormBean formBean = new ConstructSearchFormBean();
        formBean.setConstruct("mCherry lox");
        formBean.setFilter1("showAll");
        formBean.setMaxDisplayRecords(20);
        formBean.setFirstPageRecord(1);

        ConstructSearchCriteria criteria = new ConstructSearchCriteria(formBean);

        ConstructSearchResult result = RepositoryFactory.getConstructRepository().getConstructs(criteria);
        assertTrue("'pre-optic area' term search returns results", result != null && result.getResults() != null && result.getResultsFound() > 0);
    }

}
