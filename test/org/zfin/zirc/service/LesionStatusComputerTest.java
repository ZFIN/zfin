package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The group-required rule: where a lesion type demands at least one of a set
 * of fields, every member shows MISSING until one is answered.
 *
 * <p>ZFIN-10400's insertion-origin group is gone with the checklist itself;
 * what remains is ZFIN-10379's "nucleotide information OR amino acid
 * information" on a point mutation. This is the "visual indicator" that
 * ticket asks for in place of real submission validation, so it is worth
 * pinning down — particularly the ways it must stay quiet.
 */
public class LesionStatusComputerTest {

    private static Lesion lesion(String type) {
        Lesion l = new Lesion();
        l.setLesionType(type);
        return l;
    }

    @Test
    public void pointMutationWithNoNucleotideOrAminoAcidInfo_marksTheWholeGroup() {
        // ZFIN-10379's "either nucleotide information or amino acid
        // information", using the same mechanism as the ZFIN-10400 questions.
        FieldStatusResult r = LesionStatusComputer.compute(lesion("point_mutation"));

        assertEquals(FieldStatus.MISSING, r.byField().get("nucleotideChange"));
        assertEquals(FieldStatus.MISSING, r.byField().get("aaChangeFrom"));
        assertEquals(FieldStatus.MISSING, r.byField().get("aaPositionStart"));
    }

    @Test
    public void nucleotideInfoAloneSatisfiesTheAminoAcidSide() {
        Lesion l = lesion("point_mutation");
        l.setNucleotideChange("A>T");

        FieldStatusResult r = LesionStatusComputer.compute(l);

        assertEquals(FieldStatus.COMPLETE, r.byField().get("nucleotideChange"));
        // The requirement is one side or the other, so filling the nucleotide
        // side must clear the amino-acid fields rather than leaving them lit.
        assertEquals(FieldStatus.COMPLETE, r.byField().get("aaChangeFrom"));
    }

    @Test
    public void aminoAcidInfoAloneSatisfiesTheNucleotideSide() {
        Lesion l = lesion("point_mutation");
        l.setAaChangeFrom("ZDB-TERM-130401-1438");

        FieldStatusResult r = LesionStatusComputer.compute(l);

        assertEquals(FieldStatus.COMPLETE, r.byField().get("nucleotideChange"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("aaChangeTo"));
    }

    @Test
    public void theEitherOrGroupIsScopedToPointMutations() {
        // A deletion has neither box, so neither should be badged.
        FieldStatusResult r = LesionStatusComputer.compute(lesion("deletion"));

        assertEquals(FieldStatus.COMPLETE, r.byField().get("nucleotideChange"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("aaChangeFrom"));
    }

    @Test
    public void lesionTypeItselfStaysTheOnlyPerFieldRequirement() {
        // Guards the group rule against bleeding into ordinary fields: an
        // untouched lesion is missing its type and nothing else.
        FieldStatusResult r = LesionStatusComputer.compute(new Lesion());

        assertEquals(FieldStatus.MISSING,  r.byField().get("lesionType"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("deletedSequence"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("transcriptConsequences"));
    }

    /**
     * The CRISPR and TALEN sequences moved to the mutation's Mutagenesis
     * Protocol, so this computer must no longer report on them at all — a
     * stale entry here would badge a field the lesion form does not show.
     */
    @Test
    public void originChecklistFieldsAreNoLongerReported() {
        Lesion l = lesion("insertion");

        FieldStatusResult r = LesionStatusComputer.compute(l);

        for (String gone : new String[] {"insertionOrigins", "insertionOriginOther",
                "crisprSequence", "talenSequence", "constructName"}) {
            assertNull(gone + " should no longer be a lesion status field",
                    r.byField().get(gone));
        }
    }
}
