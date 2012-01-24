package org.zfin.fish.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.presentation.Fish;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.infrastructure.ZfinEntity;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FishServiceNoDBTest {

    @Test
    public void getFishIdByGenoxGeno() {
        String fishID = "ZDB-GENOX-110211-2,ZDB-GENO-110210-2,ZDB-GENOX-110211-3";
        Fish fish = FishService.getGenoGenoxByFishID(fishID);
        assertNotNull(fish);
        assertEquals(2, fish.getGenotypeExperimentIDs().size());
        assertEquals("ZDB-GENOX-110211-2,ZDB-GENOX-110211-3", fish.getGenotypeExperimentIDsString());
        assertEquals("ZDB-GENO-110210-2", fish.getGenotype().getID());
    }
}
