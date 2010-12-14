package org.zfin.feature.presentation;

import org.zfin.feature.Feature;
import org.zfin.people.Organization;

/**
 */
public class FeatureLabEntry {

    private Feature feature ;
    private Organization sourceOrganization;
    private boolean isCurrent;


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Organization getSourceOrganization() {
        return sourceOrganization;
    }

    public void setSourceOrganization(Organization sourceOrganization) {
        this.sourceOrganization = sourceOrganization;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
