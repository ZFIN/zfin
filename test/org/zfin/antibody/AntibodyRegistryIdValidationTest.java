package org.zfin.antibody;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/** ZFIN-10503: normalizing and validating Antibody Registry IDs entered by curators. */
public class AntibodyRegistryIdValidationTest {

    private static List<String> ids(String input) {
        return AntibodyService.normalizeAndValidateRegistryIDs(input);
    }

    private static String rejection(String input) {
        try {
            ids(input);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        fail("expected a rejection for: " + input);
        return null;
    }

    @Test
    public void validIdsPassThrough() {
        assertEquals(List.of("AB_528173"), ids("AB_528173"));
        assertEquals(List.of("AB_1", "AB_22"), ids("AB_1,AB_22"));
        assertEquals(List.of("AB_1", "AB_22"), ids("AB_1,   AB_22"));
    }

    @Test
    public void everyAcceptedFormBecomesAB_n() {
        for (String form : new String[]{"RRID:AB_1603723", "rrid:ab_1603723", "RRID: AB_1603723", "Ab_1603723",
                "ab_1603723", "  AB_1603723  ", "\tab_1603723\n"}) {
            assertEquals(form, List.of("AB_1603723"), ids(form));
        }
    }

    @Test
    public void emptyAndWhitespaceGiveNoIds() {
        assertEquals(List.of(), ids(null));
        assertEquals(List.of(), ids(""));
        assertEquals(List.of(), ids("   "));
        assertEquals(List.of(), ids(" , ,, "));
    }

    @Test
    public void duplicatesAreRemovedKeepingOrder() {
        assertEquals(List.of("AB_5", "AB_2"), ids("AB_5, RRID:AB_5, ab_2, AB_2, AB_5"));
    }

    @Test
    public void junkFromTheReportIsRejectedWithAClearMessage() {
        assertEquals("Invalid Antibody Registry ID \"25244-1-AP\": IDs look like AB_123456", rejection("25244-1-AP"));
        assertEquals("Invalid Antibody Registry ID \"#9722\": IDs look like AB_123456", rejection("#9722"));
        assertEquals("Invalid Antibody Registry ID \"2838892\": IDs look like AB_123456", rejection("2838892"));
        assertEquals("Invalid Antibody Registry ID \"Catlog no. did not retrieve any record\": IDs look like AB_123456",
                rejection("Catlog no. did not retrieve any record"));
    }

    @Test
    public void otherNonIdsAreRejected() {
        for (String bad : new String[]{"AB_", "AB_12x", "AB-123", "AB 123", "RRID:", "RRID:SCR_006397", "AB_1.5", "AB__1"}) {
            rejection(bad);
        }
    }

    @Test
    public void oneBadEntryRejectsTheWholeSubmissionAndNamesEveryBadEntry() {
        assertEquals("Invalid Antibody Registry ID \"junk\": IDs look like AB_123456", rejection("AB_1603723, junk"));
        assertEquals("Invalid Antibody Registry ID \"junk\": IDs look like AB_123456; "
                        + "Invalid Antibody Registry ID \"#9722\": IDs look like AB_123456",
                rejection("AB_1, junk, AB_2, #9722"));
    }

    @Test
    public void singleIdFieldNormalizesRejectsAndAllowsEmpty() {
        assertEquals("AB_1603723", AntibodyService.normalizeAndValidateRegistryID("RRID:AB_1603723"));
        assertNull(AntibodyService.normalizeAndValidateRegistryID(null));
        assertNull(AntibodyService.normalizeAndValidateRegistryID(""));
        assertNull(AntibodyService.normalizeAndValidateRegistryID("   "));
        try {
            AntibodyService.normalizeAndValidateRegistryID("25244-1-AP");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Invalid Antibody Registry ID \"25244-1-AP\": IDs look like AB_123456", expected.getMessage());
        }
        try {
            AntibodyService.normalizeAndValidateRegistryID("AB_1, AB_2");
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Enter only one Antibody Registry ID here: AB_1, AB_2", expected.getMessage());
        }
        assertEquals("a repeated ID is still one ID", "AB_1", AntibodyService.normalizeAndValidateRegistryID("AB_1, ab_1"));
    }
}
