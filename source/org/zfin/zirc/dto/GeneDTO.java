package org.zfin.zirc.dto;

import org.zfin.zirc.entity.Gene;

/**
 * Per-mutation gene record. {@code mutatedGeneZdbID} is a marker ZDB-ID
 * resolved via the {@code /api/zirc/autocomplete/markers} lookup; the
 * abbreviation is denormalized into the DTO so the React row can show
 * a human-readable label without a second fetch.
 *
 * <p>Same shape as the other aggregate DTOs ({@code MutationDTO},
 * {@code AssayDTO}): a flat record with an {@code of(entity)} static
 * factory for the controller mapping.
 */
public record GeneDTO(
        Long id,
        Long mutationId,
        Integer sortOrder,
        String mutatedGeneZdbID,
        String mutatedGeneAbbreviation,
        String linkageGroup,
        String genbankGenomicDna,
        String genbankCdna) {

    public static GeneDTO of(Gene g) {
        return new GeneDTO(
                g.getId(),
                g.getMutation() == null ? null : g.getMutation().getId(),
                g.getSortOrder(),
                g.getMutatedGene() == null ? null : g.getMutatedGene().getZdbID(),
                g.getMutatedGene() == null ? null : g.getMutatedGene().getAbbreviation(),
                g.getLinkageGroup(),
                g.getGenbankGenomicDna(),
                g.getGenbankCdna());
    }
}
