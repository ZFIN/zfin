package org.zfin.zirc.dto;

import org.zfin.zirc.entity.Lesion;

/**
 * Compact row used inside {@code MutationDTO.lesions}. The full lesion
 * record is fetched via {@code /api/zirc/lesions/{id}} when a card is
 * expanded — same pattern as {@link AssaySummaryDTO}.
 */
public record LesionSummaryDTO(Long id, Integer sortOrder, String lesionType) {

    public static LesionSummaryDTO of(Lesion l) {
        return new LesionSummaryDTO(l.getId(), l.getSortOrder(), l.getLesionType());
    }
}
