package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import static org.junit.Assert.assertEquals;

/**
 * The primer rule added for ZFIN-10407: on the assay types that show the
 * forward / reverse primer pair, a primer must be entered and must be at
 * least PRIMER_MIN_LENGTH bases.
 *
 * <p>Curators asked for a flag rather than a save-time rejection, because the
 * form autosaves as the submitter types — so the rule surfaces as
 * MISSING (nothing entered) or IN_PROGRESS (entered but too short) instead of
 * refusing the write.
 *
 * <p>The case worth pinning down hardest is the last one: ASA and KASP use the
 * WT/mutant/common primer trio instead, so a blank forward primer there is
 * correct and must not be flagged.
 */
public class GenotypingAssayStatusComputerTest {

    private static GenotypingAssay assay(String type, String forward, String reverse) {
        GenotypingAssay ga = new GenotypingAssay();
        ga.setAssayType(type);
        ga.setForwardPrimer(forward);
        ga.setReversePrimer(reverse);
        return ga;
    }

    @Test
    public void primerAtOrAboveTheMinimumIsComplete() {
        FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                assay("pcr_gel", "ACGTACGTACGT", "ACGTACGTACGT"));

        assertEquals(FieldStatus.COMPLETE, r.byField().get("forwardPrimer"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("reversePrimer"));
    }

    @Test
    public void exactlyTheMinimumIsComplete() {
        FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                assay("pcr_gel", "ACGTACGTAC", "ACGTACGTAC"));

        assertEquals(FieldStatus.COMPLETE, r.byField().get("forwardPrimer"));
    }

    @Test
    public void shortPrimerIsFlaggedInProgressNotMissing() {
        FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                assay("pcr_gel", "ACGTA", "ACGTACGTACGT"));

        assertEquals(FieldStatus.IN_PROGRESS, r.byField().get("forwardPrimer"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("reversePrimer"));
    }

    @Test
    public void missingPrimerIsMissing() {
        FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                assay("pcr_gel", null, "   "));

        assertEquals(FieldStatus.MISSING, r.byField().get("forwardPrimer"));
        assertEquals(FieldStatus.MISSING, r.byField().get("reversePrimer"));
    }

    @Test
    public void aFlaggedPrimerDragsTheOverallStatusDown() {
        FieldStatusResult complete = GenotypingAssayStatusComputer.compute(
                assay("pcr_gel", "ACGTACGTACGT", "ACGTACGTACGT"));
        FieldStatusResult short_ = GenotypingAssayStatusComputer.compute(
                assay("pcr_gel", "ACGTA", "ACGTACGTACGT"));

        assertEquals(FieldStatus.COMPLETE, complete.overall());
        assertEquals(FieldStatus.IN_PROGRESS, short_.overall());
    }

    @Test
    public void everyTypeShowingThePairIsChecked() {
        for (String type : org.zfin.zirc.api.ZircAssayFormSchema.FWD_REV_PRIMER_TYPES) {
            FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                    assay(type, "ACGTA", "ACGTACGTACGT"));

            assertEquals("short forward primer should be flagged for " + type,
                    FieldStatus.IN_PROGRESS, r.byField().get("forwardPrimer"));
        }
    }

    @Test
    public void typesUsingTheAlleleSpecificTrioAreNotFlagged() {
        // ASA and KASP never show the forward / reverse pair, so blank is right.
        for (String type : new String[] {"asa", "kasp"}) {
            FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                    assay(type, null, null));

            assertEquals("blank forward primer is correct for " + type,
                    FieldStatus.COMPLETE, r.byField().get("forwardPrimer"));
            assertEquals("blank reverse primer is correct for " + type,
                    FieldStatus.COMPLETE, r.byField().get("reversePrimer"));
        }
    }

    @Test
    public void anAssayWithNoTypeYetIsNotFlagged() {
        // assayType is the gateway field; until it is picked, the form shows no
        // primer boxes at all, so they must not be reported as missing.
        FieldStatusResult r = GenotypingAssayStatusComputer.compute(assay(null, null, null));

        assertEquals(FieldStatus.COMPLETE, r.byField().get("forwardPrimer"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("reversePrimer"));
    }
}
