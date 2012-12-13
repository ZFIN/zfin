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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganizationFeaturePrefix that = (OrganizationFeaturePrefix) o;

        if (featurePrefix != null ? !featurePrefix.equals(that.featurePrefix) : that.featurePrefix != null)
            return false;
        if (isCurrentDesignation != null ? !isCurrentDesignation.equals(that.isCurrentDesignation) : that.isCurrentDesignation != null)
            return false;
        if (organization != null ? !organization.equals(that.organization) : that.organization != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = isCurrentDesignation != null ? isCurrentDesignation.hashCode() : 0;
        result = 31 * result + (featurePrefix != null ? featurePrefix.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        return result;
    }
}
