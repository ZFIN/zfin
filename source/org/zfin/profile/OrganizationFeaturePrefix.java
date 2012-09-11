package org.zfin.profile;

import org.zfin.feature.FeaturePrefix;

import java.io.Serializable;


public class OrganizationFeaturePrefix implements Serializable{

    private Boolean isCurrentDesignation;
    private FeaturePrefix featurePrefix ;
    private Organization organization;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Boolean getCurrentDesignation() {
        return isCurrentDesignation;
    }

    public void setCurrentDesignation(Boolean currentDesignation) {
        this.isCurrentDesignation = currentDesignation;
    }

    public FeaturePrefix getFeaturePrefix() {
        return featurePrefix;
    }

    public void setFeaturePrefix(FeaturePrefix featurePrefix) {
        this.featurePrefix = featurePrefix;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LabFeaturePrefix");
        sb.append("{organization=").append(organization);
        sb.append(", isCurrentDesignation=").append(isCurrentDesignation);
        sb.append(", featurePrefix=").append(featurePrefix);
        sb.append('}');
        return sb.toString();
    }
}
