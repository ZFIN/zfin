package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.zirc.entity.Phenotype;

/**
 * Full per-phenotype payload returned by GET /api/zirc/phenotypes/{id}.
 *
 * <p>{@code segregation} and {@code type} are single-valued scalar
 * {@code text} columns (one inheritance pattern / one phenotype type per
 * phenotype). {@code stage} is a server-side display string derived
 * from {@code hpfStart} in the legacy code path; in the schema-driven
 * editor it's currently a write-through field (TODO: re-derive on
 * hpfStart change once the STAGE lookup is wired in).
 */
public record PhenotypeDTO(
        @NotNull Long id,
        @NotNull Long mutationId,
        @NotNull Integer sortOrder,
        String description,
        // Timing
        Integer hpfStart,
        Integer hpfEnd,
        String stage,
        // Image permissions
        Boolean zfinImagePermission,
        Boolean zircImagePermission,
        // Non-Mendelian segregation
        Double nonMendelianPercentage,
        String nonMendelianComment,
        // Single-valued scalar text columns
        String segregation,
        String type) {

    public static PhenotypeDTO of(Phenotype p) {
        return new PhenotypeDTO(
                p.getId(),
                p.getMutation() == null ? null : p.getMutation().getId(),
                p.getSortOrder(),
                p.getDescription(),
                p.getHpfStart(),
                p.getHpfEnd(),
                p.getStage(),
                p.getZfinImagePermission(),
                p.getZircImagePermission(),
                p.getNonMendelianPercentage(),
                p.getNonMendelianComment(),
                p.getSegregation(),
                p.getType());
    }
}
