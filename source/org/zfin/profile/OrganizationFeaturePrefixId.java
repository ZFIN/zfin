package org.zfin.profile;

import java.io.Serializable;
import java.util.Objects;

public class OrganizationFeaturePrefixId implements Serializable {
    private String organization;
    private int featurePrefix;
    private boolean currentDesignation;

    public OrganizationFeaturePrefixId() {
    }

    public OrganizationFeaturePrefixId(String organization, int featurePrefix, boolean currentDesignation) {
        this.organization = organization;
        this.featurePrefix = featurePrefix;
        this.currentDesignation = currentDesignation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrganizationFeaturePrefixId that = (OrganizationFeaturePrefixId) o;
        return currentDesignation == that.currentDesignation &&
                Objects.equals(organization, that.organization) &&
                featurePrefix == that.featurePrefix;
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, featurePrefix, currentDesignation);
    }
}
