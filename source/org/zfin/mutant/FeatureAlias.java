package org.zfin.mutant;

import org.zfin.infrastructure.DataAlias;
import org.zfin.mutant.Feature;


public class FeatureAlias extends DataAlias {

    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}
