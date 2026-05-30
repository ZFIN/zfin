package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.zirc.entity.Phenotype;

/**
 * Compact row used inside {@code MutationDTO.phenotypes}. The full
 * phenotype record is fetched via {@code /api/zirc/phenotypes/{id}}
 * when a card is expanded — same pattern as {@link LesionSummaryDTO}.
 *
 * <p>{@code description} is truncated by the renderer for display;
 * the full text comes back on expand.
 */
public record PhenotypeSummaryDTO(
        @NotNull Long id,
        @NotNull Integer sortOrder,
        String description) {

    public static PhenotypeSummaryDTO of(Phenotype p) {
        return new PhenotypeSummaryDTO(p.getId(), p.getSortOrder(), p.getDescription());
    }
}
