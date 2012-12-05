package org.zfin.mutant.repository;

import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.WarehouseSummary;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.SortBy;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
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



}
