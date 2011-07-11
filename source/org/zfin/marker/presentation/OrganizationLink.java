package org.zfin.marker.presentation;

import org.apache.log4j.Logger;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.people.presentation.SourcePresentation;
import org.zfin.properties.ZfinProperties;


/**
 */
public class OrganizationLink implements ProvidesLink {

    private Logger logger = Logger.getLogger(OrganizationLink.class);

    private String supplierZdbId;
    private String sourceUrl;
    private String urlDisplayText;
    private String accessionNumber;
    private String companyName;
    private String labName;

    @Override
    public String getLink() {
        if (supplierZdbId.startsWith("ZDB-LAB-")) {
            return "<a href=\"/" + ZfinProperties.getWebDriver()  + SourcePresentation.LAB_URI + supplierZdbId + "\">" + labName + "</a>";
        } else if (supplierZdbId.startsWith("ZDB-COMPANY-")) {
            return "<a href=\"/" + ZfinProperties.getWebDriver()  + SourcePresentation.COMANY_URI + supplierZdbId + "\">" + companyName + "</a>";
        } else {
            logger.error("bad zdbID for Organization Link[" + supplierZdbId + "]");
            return null;
        }
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        String link = getLink() ;
        String orderThisLink = getOrderThisLink();
        return link + (orderThisLink!=null ? " "+orderThisLink : "");
    }

    public String getOrderThisLink() {
        if(supplierZdbId==null) return null ;
        if (sourceUrl != null) {
            return "<span style=\"font-size: small;\">(<a href=\"" + sourceUrl
                    + (accessionNumber!=null ? accessionNumber : "")
                    + "\">" + urlDisplayText + "</a>)</span>";
        } else if (supplierZdbId.startsWith("ZDB-LAB-")) {
            return "<span style=\"font-size: small;\">(<a href=\"/" + ZfinProperties.getWebDriver() + SourcePresentation.LAB_URI + supplierZdbId + "\">order this</a>)</span>";
        } else if (supplierZdbId.startsWith("ZDB-COMPANY-")) {
            return "<span style=\"font-size: small;\">(<a href=\"/" + ZfinProperties.getWebDriver()  + SourcePresentation.COMANY_URI + supplierZdbId + "\">order this</a>)</span>";
        } else {
            logger.error("bad zdbID for Organization Link[" + supplierZdbId + "]");
            return null;
        }
    }

    public String getSupplierZdbId() {
        return supplierZdbId;
    }

    public void setSupplierZdbId(String supplierZdbId) {
        this.supplierZdbId = supplierZdbId;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getUrlDisplayText() {
        return urlDisplayText;
    }

    public void setUrlDisplayText(String urlDisplayText) {
        this.urlDisplayText = urlDisplayText;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OrganizationLink");
        sb.append("{supplierZdbId='").append(supplierZdbId).append('\'');
        sb.append(", urlDisplayText='").append(urlDisplayText).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrganizationLink that = (OrganizationLink) o;

        if (supplierZdbId != null ? !supplierZdbId.equals(that.supplierZdbId) : that.supplierZdbId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return supplierZdbId != null ? supplierZdbId.hashCode() : 0;
    }
}
