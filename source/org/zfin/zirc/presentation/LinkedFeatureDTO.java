package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.LinkedFeature;

/**
 * Wire format for one {@link LinkedFeature} row. {@code feature} is the
 * row's identity (composite-PK component on the entity); the other fields
 * are nullable distance / annotation metadata.
 */
@Getter
@Setter
@NoArgsConstructor
public class LinkedFeatureDTO {

    private String feature;
    private Boolean distanceKnown;
    private Double distanceCentimorgans;
    private Double distanceMegabases;
    private String additionalInfo;

    public static LinkedFeatureDTO from(LinkedFeature lf) {
        LinkedFeatureDTO dto = new LinkedFeatureDTO();
        dto.setFeature(lf.getFeature());
        dto.setDistanceKnown(lf.getDistanceKnown());
        dto.setDistanceCentimorgans(lf.getDistanceCentimorgans());
        dto.setDistanceMegabases(lf.getDistanceMegabases());
        dto.setAdditionalInfo(lf.getAdditionalInfo());
        return dto;
    }
}
