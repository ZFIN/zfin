package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.LinkedFeature;
import org.zfin.zirc.entity.Mutation;

/**
 * Wire format for one {@link LinkedFeature} pair. Identifies two of the
 * submission's mutations plus distance metadata between them. The label
 * fields are server-supplied display echoes (allele designation, falling
 * back to "#sortOrder" when blank); the client doesn't send them back.
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
    private Double distanceCentimorgans;
    private Double distanceMegabases;
    private String additionalInfo;

    public static LinkedFeatureDTO from(LinkedFeature lf) {
        LinkedFeatureDTO dto = new LinkedFeatureDTO();
        dto.setMutationAId(lf.getMutationA() != null ? lf.getMutationA().getId() : null);
        dto.setMutationBId(lf.getMutationB() != null ? lf.getMutationB().getId() : null);
        dto.setMutationALabel(labelFor(lf.getMutationA()));
        dto.setMutationBLabel(labelFor(lf.getMutationB()));
        dto.setDistanceKnown(lf.getDistanceKnown());
        dto.setDistanceCentimorgans(lf.getDistanceCentimorgans());
        dto.setDistanceMegabases(lf.getDistanceMegabases());
        dto.setAdditionalInfo(lf.getAdditionalInfo());
        return dto;
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
