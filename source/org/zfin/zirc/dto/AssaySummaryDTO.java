package org.zfin.zirc.dto;

import org.zfin.zirc.entity.GenotypingAssay;

/**
 * Lightweight per-assay row sent inside {@link MutationDTO#assays()}.
 * Drives the collapsed-card header in AssaysListRenderer — the full
 * per-assay field set is fetched separately when a card is expanded.
 */
public record AssaySummaryDTO(
        Long id,
        Integer sortOrder,
        String assayType) {

    public static AssaySummaryDTO of(GenotypingAssay a) {
        return new AssaySummaryDTO(a.getId(), a.getSortOrder(), a.getAssayType());
    }
}
