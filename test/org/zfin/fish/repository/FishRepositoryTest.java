package org.zfin.fish.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.fish.WarehouseSummary;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.repository.RepositoryFactory;

import java.util.Set;

import static org.junit.Assert.assertNotNull;

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
