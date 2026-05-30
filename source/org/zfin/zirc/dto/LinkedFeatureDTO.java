package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.zirc.entity.LinkedFeature;

/**
 * One linkage relationship between two mutations on the same submission.
 *
 * <p>Distance is stored on the entity as two separate columns —
 * {@code distanceCentimorgans} and {@code distanceMegabases} — only one
 * of which is non-null at a time. The DTO carries both; the React
 * renderer presents them as a single (value, unit) combo, and the
 * field-path PATCH targets whichever column the chosen unit maps to.
 */
public record LinkedFeatureDTO(
        @NotNull Long mutationAId,
        @NotNull Long mutationBId,
        Boolean distanceKnown,
        Double distanceCentimorgans,
        Double distanceMegabases,
        String additionalInfo) {

    public static LinkedFeatureDTO of(LinkedFeature lf) {
        return new LinkedFeatureDTO(
                lf.getMutationA() == null ? null : lf.getMutationA().getId(),
                lf.getMutationB() == null ? null : lf.getMutationB().getId(),
                lf.getDistanceKnown(),
                lf.getDistanceCentimorgans(),
                lf.getDistanceMegabases(),
                lf.getAdditionalInfo());
    }
}
