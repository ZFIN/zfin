package org.zfin.mapping;

import org.zfin.feature.Feature;

/**
 *
 */
public class FeatureSingletonLinkage extends SingletonLinkage {

    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        entity = feature;
    }

}
