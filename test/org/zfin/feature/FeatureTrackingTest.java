package org.zfin.feature;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;

public class FeatureTrackingTest extends AbstractDatabaseTest {

    /**
     * Test that the next ZFIN number is not already in use by getting the next line and then
     * try to retrieve it from the database feature tracking table.
     */
    @Test
    public void featureTrackingNextZfinNumberTest() {
        String nextLine = getFeatureRepository().getNextZFLineNum();
        FeatureTracking ft = getFeatureRepository().getFeatureTrackingByAbbreviation("zf" + nextLine);
        assertNull(ft);
    }

}

