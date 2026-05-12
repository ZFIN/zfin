package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.LinkedFeature;
import org.zfin.zirc.entity.Mutation;

/**
 * Wire format for one {@link LinkedFeature} pair. Identifies two of the
 * submission's mutations plus distance metadata between them.
 *
 * <p>The mutation label fields are server-supplied display echoes
 * (allele designation, falling back to "#sortOrder" when blank); the
 * client doesn't send them back.
 *
 * <p>Distance is exposed on the wire as a unified
 * {@code distanceValue, distanceUnit} pair, even though storage keeps
 * them split across two columns
 * ({@code distance_centimorgans, distance_megabases}). The DTO
 * conversion handles the mapping; clients see one number + one unit.
 * {@code distanceUnit} is {@code "cM"} or {@code "Mb"}; anything else
 * stored along with a non-null value is treated as "no distance" on
 * write.
 */
@Getter
@Setter
@NoArgsConstructor
public class LinkedFeatureDTO {

    private Long mutationAId;
    private Long mutationBId;
    private String mutationALabel;
    private String mutationBLabel;
    private Boolean distanceKnown;
    private Double distanceValue;
    private String distanceUnit;
    private String additionalInfo;

    public static LinkedFeatureDTO from(LinkedFeature lf) {
        LinkedFeatureDTO dto = new LinkedFeatureDTO();
        dto.setMutationAId(lf.getMutationA() != null ? lf.getMutationA().getId() : null);
        dto.setMutationBId(lf.getMutationB() != null ? lf.getMutationB().getId() : null);
        dto.setMutationALabel(labelFor(lf.getMutationA()));
        dto.setMutationBLabel(labelFor(lf.getMutationB()));
        dto.setDistanceKnown(lf.getDistanceKnown());
        if (lf.getDistanceCentimorgans() != null) {
            dto.setDistanceValue(lf.getDistanceCentimorgans());
            dto.setDistanceUnit("cM");
        } else if (lf.getDistanceMegabases() != null) {
            dto.setDistanceValue(lf.getDistanceMegabases());
            dto.setDistanceUnit("Mb");
        }
        dto.setAdditionalInfo(lf.getAdditionalInfo());
        return dto;
    }

    /**
     * Apply the DTO's distance fields to the matching entity columns,
     * clearing the column that doesn't correspond to the picked unit so
     * stale values don't linger when the curator switches units.
     */
    public void applyDistanceTo(LinkedFeature lf) {
        if (distanceValue == null || distanceUnit == null) {
            lf.setDistanceCentimorgans(null);
            lf.setDistanceMegabases(null);
            return;
        }
        if ("cM".equals(distanceUnit)) {
            lf.setDistanceCentimorgans(distanceValue);
            lf.setDistanceMegabases(null);
        } else if ("Mb".equals(distanceUnit)) {
            lf.setDistanceCentimorgans(null);
            lf.setDistanceMegabases(distanceValue);
        } else {
            // Unrecognised unit: treat as "no distance" rather than
            // silently miscategorising the value.
            lf.setDistanceCentimorgans(null);
            lf.setDistanceMegabases(null);
        }
    }

    private static String labelFor(Mutation m) {
        if (m == null) {
            return null;
        }
        String allele = m.getAlleleDesignation();
        if (allele != null && !allele.isBlank()) {
            return allele;
        }
        return "#" + m.getSortOrder();
    }
}
