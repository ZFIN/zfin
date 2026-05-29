package org.zfin.feature;

import org.zfin.gwt.root.dto.FeatureTypeEnum;

/**
 * Projection returned by SequenceRepository.getDeletionSizeDriftCandidates() —
 * the columns needed to verify a feature's stored deletion size
 * (FeatureDnaMutationDetail.numberRemovedBasePair) against the size implied
 * by its chromosome coordinates on FeatureLocation. A feature with no
 * FeatureLocation still appears here (assembly/chromosome/start/end may all
 * be null); the caller decides whether to skip those.
 */
public record FeatureDeletionSizeRow(
        String featureZdbId,
        String featureAbbrev,
        FeatureTypeEnum featureType,
        String assemblyName,
        String chromosome,
        Integer startLocation,
        Integer endLocation,
        Integer storedSize
) {
}