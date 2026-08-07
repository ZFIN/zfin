package org.zfin.zirc.service;

import org.junit.Test;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import static org.junit.Assert.assertEquals;

/**
 * The group-required rule added for ZFIN-10400: on an insertion, at least one
 * of the two origin questions must be answered, and until one is, both show
 * MISSING.
 *
 * <p>This is the "visual indicator" the ticket asks for in place of real
 * submission validation, so it is worth pinning down — particularly the two
 * ways it must stay quiet (wrong lesion type, and one answer being enough).
 */
public class LesionStatusComputerTest {

    private static Lesion lesion(String type) {
        Lesion l = new Lesion();
        l.setLesionType(type);
        return l;
    }

    @Test
    public void insertionWithNeitherOriginQuestionAnswered_marksBoth() {
        FieldStatusResult r = LesionStatusComputer.compute(lesion("insertion"));

        assertEquals(FieldStatus.MISSING, r.byField().get("insertionFromMutagenesis"));
        assertEquals(FieldStatus.MISSING, r.byField().get("insertionFromConstruct"));
    }

    @Test
    public void answeringEitherQuestionClearsBoth() {
        Lesion l = lesion("insertion");
        l.setInsertionFromMutagenesis(Boolean.TRUE);

        FieldStatusResult r = LesionStatusComputer.compute(l);

        assertEquals(FieldStatus.COMPLETE, r.byField().get("insertionFromMutagenesis"));
        // The requirement is "one of the two", so the unanswered sibling is
        // satisfied by its partner rather than being separately required.
        assertEquals(FieldStatus.COMPLETE, r.byField().get("insertionFromConstruct"));
    }

    @Test
    public void answeringNoStillCounts() {
        // "No" is an answer. Only null is unanswered, which is why the columns
        // are nullable booleans rather than defaulting to false.
        Lesion l = lesion("insertion");
        l.setInsertionFromConstruct(Boolean.FALSE);

        FieldStatusResult r = LesionStatusComputer.compute(l);

        assertEquals(FieldStatus.COMPLETE, r.byField().get("insertionFromMutagenesis"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("insertionFromConstruct"));
    }

    @Test
    public void otherLesionTypesAreNotAsked() {
        // The questions aren't rendered for a deletion, so badging them would
        // point at fields the curator cannot see.
        FieldStatusResult r = LesionStatusComputer.compute(lesion("deletion"));

        assertEquals(FieldStatus.COMPLETE, r.byField().get("insertionFromMutagenesis"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("insertionFromConstruct"));
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
        // untouched lesion is missing its type and nothing else beyond the
        // origin questions.
        FieldStatusResult r = LesionStatusComputer.compute(new Lesion());

        assertEquals(FieldStatus.MISSING,  r.byField().get("lesionType"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("deletedSequence"));
        assertEquals(FieldStatus.COMPLETE, r.byField().get("transcriptConsequences"));
        // Null lesion type matches no group, so the origin questions are quiet.
        assertEquals(FieldStatus.COMPLETE, r.byField().get("insertionFromMutagenesis"));
    }
}
