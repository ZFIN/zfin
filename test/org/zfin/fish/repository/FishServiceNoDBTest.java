package org.zfin.fish.repository;

import org.junit.Test;
import org.zfin.fish.presentation.MartFish;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FishServiceNoDBTest {

    @Test
    public void getFishIdByGenoxGeno() {
        String fishID = "ZDB-GENOX-110211-2,ZDB-GENO-110210-2,ZDB-GENOX-110211-3";
        MartFish fish = FishService.getGenoGenoxByFishID(fishID);
        assertNotNull(fish);
        assertEquals(2, fish.getGenotypeExperimentIDs().size());
        assertEquals("ZDB-GENOX-110211-2,ZDB-GENOX-110211-3", fish.getGenotypeExperimentIDsString());
        assertEquals("ZDB-GENO-110210-2", fish.getGenotype().getID());
    }
}
