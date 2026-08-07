package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.zirc.entity.Lesion;

/**
 * Full per-lesion payload returned by GET /api/zirc/lesions/{id}. Every
 * column in {@code zirc.lesion} is reachable here so the per-lesion
 * schema editor (M7.1) can read & write any field; the uiSchema's
 * conditional-show rules decide what's <em>visible</em>, the
 * persistence layer doesn't.
 *
 * <p>Mirrors {@link AssayDTO}'s shape — lesion-type matrix works the
 * same way assay-type matrix does, just with a different set of
 * clusters.
 */
public record LesionDTO(
        @NotNull Long id,
        @NotNull Long mutationId,
        @NotNull Integer sortOrder,
        String lesionType,
        // Size fields (per-type)
        Integer lesionSizeBp,
        Integer insertionSizeBp,
        // Sequence specifics
        String nucleotideChange,
        String deletedSequence,
        String insertedSequence,
        String transgeneSequence,
        // Location
        String locationInline,
        String fivePrimeFlank,
        String threePrimeFlank,
        Boolean hasLargeVariant,
        // Insertion origin (ZFIN-10400)
        Boolean insertionFromMutagenesis,
        Boolean insertionFromConstruct,
        String crisprSequence,
        String talenSequence,
        String constructName,
        // Protein-level
        String mutatedAminoAcids,
        String mutatedAminoAcidsHgvs,
        // Transcript-level: mdcv term ZDB IDs (ZFIN-10399)
        String[] transcriptConsequences,
        // Catch-all
        String additionalInfo) {

    public static LesionDTO of(Lesion l) {
        return new LesionDTO(
                l.getId(),
                l.getMutation() == null ? null : l.getMutation().getId(),
                l.getSortOrder(),
                l.getLesionType(),
                l.getLesionSizeBp(),
                l.getInsertionSizeBp(),
                l.getNucleotideChange(),
                l.getDeletedSequence(),
                l.getInsertedSequence(),
                l.getTransgeneSequence(),
                l.getLocationInline(),
                l.getFivePrimeFlank(),
                l.getThreePrimeFlank(),
                l.getHasLargeVariant(),
                l.getInsertionFromMutagenesis(),
                l.getInsertionFromConstruct(),
                l.getCrisprSequence(),
                l.getTalenSequence(),
                l.getConstructName(),
                l.getMutatedAminoAcids(),
                l.getMutatedAminoAcidsHgvs(),
                l.getTranscriptConsequences() == null
                        ? new String[0] : l.getTranscriptConsequences(),
                l.getAdditionalInfo());
    }
}
