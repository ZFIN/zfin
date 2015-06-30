package org.zfin.fish.repository;

import org.hibernate.Query;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.FunctionalAnnotation;
import org.zfin.fish.WarehouseSummary;
import org.zfin.mutant.Fish;
import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.SortBy;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.zfin.repository.RepositoryFactory.getFishRepository;

public class FishRepositoryTest extends AbstractDatabaseTest {


    @Test
    public void getPhenotypeFigures() {
        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getFishRepository().getAllFigures("ZDB-GENOX-100423-2");
        assertNotNull(zfinFigureEntities);
    }

    @Test
    public void getPhenotypeFiguresForSingleTerm() {
        // brain
        String termID = "ZDB-TERM-100331-8";
        // WT (unspecified) + MO1-acd
        String fishID = "ZDB-GENO-030619-2,ZDB-GENOX-110325-3";
        List<String> termList = new ArrayList<String>(1);
        termList.add(termID);

        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getFishRepository().getFiguresByFishAndTerms(fishID, termList);
        assertNotNull(zfinFigureEntities);
    }

    @Test
    public void getPhenotypeFiguresForMultipleTerms() {
        // brain nucleus
        String brainNucleus = "ZDB-TERM-110313-4";
        // eye
        String eye = "ZDB-TERM-100331-100";
        // gli2aty17a/ty17a
        String fishID = "ZDB-GENO-980202-1115,ZDB-GENOX-041102-68,ZDB-GENOX-081006-2";
        List<String> termList = new ArrayList<String>(2);
        termList.add(brainNucleus);
        termList.add(eye);

        Set<ZfinFigureEntity> zfinFigureEntities = RepositoryFactory.getFishRepository().getFiguresByFishAndTerms(fishID, termList);
        assertNotNull(zfinFigureEntities);
        assertTrue(zfinFigureEntities.size() >= 2);
    }



    @Test
    public void warehouseReleaseTrackingInfo() {
        WarehouseSummary summary = RepositoryFactory.getFishRepository().getWarehouseSummary(WarehouseSummary.Mart.FISH_MART);
        assertNotNull(summary);
    }




}
