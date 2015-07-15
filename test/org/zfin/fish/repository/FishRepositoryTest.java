package org.zfin.fish.repository;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
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
    public void warehouseReleaseTrackingInfo() {
        WarehouseSummary summary = RepositoryFactory.getFishRepository().getWarehouseSummary(WarehouseSummary.Mart.FISH_MART);
        assertNotNull(summary);
    }




}
