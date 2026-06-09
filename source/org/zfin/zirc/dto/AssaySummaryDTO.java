package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.zirc.entity.GenotypingAssay;

/**
 * Lightweight per-assay row sent inside {@link MutationDTO#assays()}.
 * Drives the collapsed-card header in AssaysListRenderer — the full
 * per-assay field set is fetched separately when a card is expanded.
 */
public record AssaySummaryDTO(
        @NotNull Long id,
        @NotNull Integer sortOrder,
        String assayType) {

    public static AssaySummaryDTO of(GenotypingAssay a) {
        return new AssaySummaryDTO(a.getId(), a.getSortOrder(), a.getAssayType());
    }
}
