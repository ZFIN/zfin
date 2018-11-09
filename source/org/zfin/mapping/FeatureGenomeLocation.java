package org.zfin.mapping;

import org.zfin.feature.Feature;

/**
 * Genomic location info for a feature.
 */
public class FeatureGenomeLocation extends GenomeLocation {

    private Feature feature;
    private String assemblyNum;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        this.entityID = feature.getZdbID();
    }




}
