package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.zirc.api.ZircAssayFormSchema;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import static org.junit.Assert.assertEquals;

/**
 * The primer rule added for ZFIN-10407 and widened by ZFIN-10439: on the assay
 * types that show a primer box, the primer must be entered and must be at
 * least PRIMER_MIN_LENGTH bases. That is the forward / reverse pair on the six
 * PCR-style types, and the WT/mutant/common trio on ASA and KASP.
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

    private static final String[] TRIO =
            {"wtSpecificPrimer", "mutSpecificPrimer", "commonPrimer"};

    private static GenotypingAssay assay(String type, String forward, String reverse) {
        GenotypingAssay ga = new GenotypingAssay();
        ga.setAssayType(type);
        ga.setForwardPrimer(forward);
        ga.setReversePrimer(reverse);
        return ga;
    }

    /** An ASA/KASP-shaped assay: the trio filled in, no forward / reverse pair. */
    private static GenotypingAssay trioAssay(String type, String wt, String mut, String common) {
        GenotypingAssay ga = new GenotypingAssay();
        ga.setAssayType(type);
        ga.setWtSpecificPrimer(wt);
        ga.setMutSpecificPrimer(mut);
        ga.setCommonPrimer(common);
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
        for (String type : ZircAssayFormSchema.FWD_REV_PRIMER_TYPES) {
            FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                    assay(type, "ACGTA", "ACGTACGTACGT"));

            assertEquals("short forward primer should be flagged for " + type,
                    FieldStatus.IN_PROGRESS, r.byField().get("forwardPrimer"));
        }
    }

    @Test
    public void typesUsingTheAlleleSpecificTrioAreNotFlaggedOnTheFwdRevPair() {
        // ASA and KASP never show the forward / reverse pair, so blank is right.
        for (String type : ZircAssayFormSchema.ALLELE_SPECIFIC_TYPES) {
            FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                    assay(type, null, null));

            assertEquals("blank forward primer is correct for " + type,
                    FieldStatus.COMPLETE, r.byField().get("forwardPrimer"));
            assertEquals("blank reverse primer is correct for " + type,
                    FieldStatus.COMPLETE, r.byField().get("reversePrimer"));
        }
    }

    // --- ZFIN-10439: the same minimum on the ASA / KASP primer trio ---

    @Test
    public void theTrioIsLengthCheckedOnEveryAlleleSpecificType() {
        for (String type : ZircAssayFormSchema.ALLELE_SPECIFIC_TYPES) {
            FieldStatusResult ok = GenotypingAssayStatusComputer.compute(
                    trioAssay(type, "ACGTACGTAC", "ACGTACGTAC", "ACGTACGTACGT"));
            for (String field : TRIO) {
                assertEquals(field + " at the minimum is complete for " + type,
                        FieldStatus.COMPLETE, ok.byField().get(field));
            }

            FieldStatusResult short_ = GenotypingAssayStatusComputer.compute(
                    trioAssay(type, "ACGTA", "ACGTACGTAC", "ACGTACGTAC"));
            assertEquals("a short WT-specific primer is flagged for " + type,
                    FieldStatus.IN_PROGRESS, short_.byField().get("wtSpecificPrimer"));
            assertEquals(FieldStatus.IN_PROGRESS, short_.overall());
        }
    }

    @Test
    public void aBlankTrioIsMissingOnAlleleSpecificTypes() {
        FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                trioAssay("asa", null, "", "   "));

        for (String field : TRIO) {
            assertEquals(field + " should be reported missing",
                    FieldStatus.MISSING, r.byField().get(field));
        }
    }

    @Test
    public void theTrioIsNotFlaggedOnTypesThatDoNotShowIt() {
        // The six PCR-style types show the forward / reverse pair instead, so a
        // blank trio there is correct — this is the mirror of the ASA case above
        // and the reason the rule is keyed by field *and* type.
        for (String type : ZircAssayFormSchema.FWD_REV_PRIMER_TYPES) {
            FieldStatusResult r = GenotypingAssayStatusComputer.compute(
                    assay(type, "ACGTACGTAC", "ACGTACGTAC"));

            for (String field : TRIO) {
                assertEquals("blank " + field + " is correct for " + type,
                        FieldStatus.COMPLETE, r.byField().get(field));
            }
            assertEquals(FieldStatus.COMPLETE, r.overall());
        }
    }

    @Test
    public void anAssayWithNoTypeYetIsNotFlagged() {
        // assayType is the gateway field; until it is picked, the form shows no
        // primer boxes at all, so they must not be reported as missing.
        FieldStatusResult r = GenotypingAssayStatusComputer.compute(assay(null, null, null));

        assertEquals(FieldStatus.COMPLETE, r.byField().get("forwardPrimer"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("reversePrimer"));
        for (String field : TRIO) {
            assertEquals(FieldStatus.COMPLETE, r.byField().get(field));
        }
    }
}
