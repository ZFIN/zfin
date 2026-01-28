package org.zfin.profile;

import java.io.Serializable;
import java.util.Objects;

public class SourceUrlId implements Serializable {
    private String organization;
    private String urlPrefix;
    private String businessPurpose;

    public SourceUrlId() {
    }

    public SourceUrlId(String organization, String urlPrefix, String businessPurpose) {
        this.organization = organization;
        this.urlPrefix = urlPrefix;
        this.businessPurpose = businessPurpose;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SourceUrlId that = (SourceUrlId) o;
        return Objects.equals(organization, that.organization) &&
                Objects.equals(urlPrefix, that.urlPrefix) &&
                Objects.equals(businessPurpose, that.businessPurpose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, urlPrefix, businessPurpose);
    }
}
