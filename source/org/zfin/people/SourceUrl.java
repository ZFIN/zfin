package org.zfin.people;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * This class identifies a single url and its business purpose for an organization.
 */
public class SourceUrl implements Serializable {

    private Organization organization;
    private String urlPrefix;
    private String hyperlinkName;
    // ToDo: turn this into enumeration
    private String businessPurpose;

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getHyperlinkName() {
        return hyperlinkName;
    }

    public void setHyperlinkName(String hyperlinkName) {
        this.hyperlinkName = hyperlinkName;
    }

    public String getBusinessPurpose() {
        return businessPurpose;
    }

    public void setBusinessPurpose(String businessPurpose) {
        this.businessPurpose = businessPurpose;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SourceUrl))
            return false;
        SourceUrl url = (SourceUrl) o;
        return
                ObjectUtils.equals(organization, url.getOrganization()) &&
                        StringUtils.equals(businessPurpose, url.getBusinessPurpose()) &&
                        StringUtils.equals(hyperlinkName, url.getHyperlinkName()) &&
                        StringUtils.equals(urlPrefix, url.getUrlPrefix());
    }

    @Override
    public int hashCode() {
        int hash = 37;
        if (organization != null)
            hash = hash * organization.hashCode();
        if(businessPurpose != null)
            hash += hash * businessPurpose.hashCode();
        if(hyperlinkName != null)
            hash += hash * hyperlinkName.hashCode();
        if(urlPrefix != null)
            hash += hash * urlPrefix.hashCode();
        return hash;
    }

    public static enum BusinessPurpose {
        ORDER_THIS("order"),
        INFORMATION("information");

        private final String value;

        private BusinessPurpose(String val) {
            value = val;
        }

        public String toString() {
            return this.value;
        }

        public static BusinessPurpose getType(String purpose) {
            for (BusinessPurpose t : values()) {
                if (t.toString().equals(purpose))
                    return t;
            }
            throw new RuntimeException("No business purpose of string " + purpose + " found.");
        }
    }
}
