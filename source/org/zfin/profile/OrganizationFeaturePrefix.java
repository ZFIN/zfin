package org.zfin.profile;

import jakarta.persistence.*;
import org.zfin.feature.FeaturePrefix;

import java.io.Serializable;

@Entity
@Table(name = "source_feature_prefix")
@IdClass(OrganizationFeaturePrefixId.class)
public class OrganizationFeaturePrefix implements Serializable {

    @Id
    @Column(name = "sfp_current_designation")
    private Boolean currentDesignation;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sfp_prefix_id")
    private FeaturePrefix featurePrefix;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sfp_source_zdb_id")
    private Organization organization;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Boolean getCurrentDesignation() {
        return currentDesignation;
    }

    public void setCurrentDesignation(Boolean currentDesignation) {
        this.currentDesignation = currentDesignation;
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
        sb.append(", currentDesignation=").append(currentDesignation);
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
        if (currentDesignation != null ? !currentDesignation.equals(that.currentDesignation) : that.currentDesignation != null)
            return false;
        if (organization != null ? !organization.equals(that.organization) : that.organization != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = currentDesignation != null ? currentDesignation.hashCode() : 0;
        result = 31 * result + (featurePrefix != null ? featurePrefix.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        return result;
    }
}
