package org.zfin.feature;

import org.zfin.infrastructure.DataAlias;


public class FeatureAlias extends DataAlias {

    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
