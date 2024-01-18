package org.zfin.feature;

import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.profile.Lab;

import java.util.List;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getFeatureRepository;
import static org.zfin.repository.RepositoryFactory.getProfileRepository;

public class FeatureTrackingTest extends AbstractDatabaseTest {

    /**
     * Test that the next ZFIN number is not already in use by getting the next line and then
     * try to retrieve it from the database feature tracking table.
     */
    @Test
    public void featureTrackingNextZfinNumberTest() {
        String nextLine = getFeatureRepository().getNextZFLineNum();

        int asNumber = Integer.parseInt(nextLine);
        assertTrue(asNumber > 3000);

        boolean exists = getFeatureRepository().isExistingFeatureTrackingByAbbreviation("zf" + nextLine);
        assertFalse(exists);
    }

    @Test
    public void getNextLineNumberForArbitraryLab() {
        String labID = "ZDB-LAB-000114-8";
        Lab lab = getProfileRepository().getLabById(labID);
        assertEquals("Halloran Lab", lab.getName());
        List<FeaturePrefix> labPrefixes = getFeatureRepository().getLabPrefixesById(labID, false);
        assertEquals(1, labPrefixes.size());
        FeaturePrefix labPrefix = labPrefixes.get(0);
        String prefix = labPrefix.getAbbreviation();
        assertEquals("uw", prefix);

        String nextLine = getFeatureRepository().getNextLineNumberForLabPrefix(labPrefix);
        int asNumber = Integer.parseInt(nextLine);
        assertTrue(asNumber > 8000);
    }

    @Test
    @Ignore
    public void getNextLineNumberForAllLabs() {
        List<Lab> labs = getProfileRepository().getLabs();
        for (Lab lab : labs) {
            List<FeaturePrefix> labPrefixes = getFeatureRepository().getLabPrefixesById(lab.getZdbID(), false);
            for (FeaturePrefix labPrefix : labPrefixes) {
                try {
                    String nextLine = getFeatureRepository().getNextLineNumberForLabPrefix(labPrefix);
                    int asNumber = Integer.parseInt(nextLine);

                    assertTrue(asNumber > 0);
                    if (asNumber == 1) {
                        System.out.println("Lab: " + lab.getName() + " Prefix: " + labPrefix.getAbbreviation() + " Next Line: " + nextLine);
                        System.out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("!! Exception caught Lab: " + lab.getName() + " Prefix: " + labPrefix.getAbbreviation() );
                    System.out.println(e.getMessage());
                    System.out.flush();
                    fail("Exception caught Lab: " + lab.getName() + " Prefix: " + labPrefix.getAbbreviation() + " " + e.getMessage());
                }
            }
        }
    }

}

