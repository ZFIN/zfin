package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.publication.presentation.PublicationPresentation;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class MarkerRelationshipPresentation implements ProvidesLink {
    private boolean is1To2;
    private String relationshipType;
    private String abbreviation;
    private String abbreviationOrder;
    private String zdbId; // id of the thing you are on
    private String markerRelationshipZdbId ; // primary key
    private String markerType;
    private String markerRelationshipAttributionPubZdbId;
    private int numAttributions;
    private String supplierZdbId;
    private String link;
    private String attributionZdbID;
    private Set<String> attributionZdbIDs = new HashSet<String>(); // TODO: implement with munging if needed
    private String arbitraryOrder;
    private String mappedMarkerRelationshipType;
    private Set<OrganizationLink> organizationLinks = new HashSet<OrganizationLink>();

    @Override
    public String getLinkWithAttribution() {
        String link = getLink();
        String attributionLink = getAttributionLink();
        return link
                + (StringUtils.isNotEmpty(attributionLink) ? " " + attributionLink : "")
                ;
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        String link = getLink();
        String attributionLink = getAttributionLink();
        String orderThisLink = getOrderThisLink();
        return link
                + (StringUtils.isNotEmpty(attributionLink) ? " " + attributionLink : "")
                + (StringUtils.isNotEmpty(orderThisLink) ? " " + orderThisLink : "")
                ;
    }

    public String getAttributionLink() {
        StringBuilder sb = new StringBuilder("");

        if (attributionZdbIDs.size() == 1) {
            sb.append(" (");
            sb.append(PublicationPresentation.getLink(attributionZdbID, "1"));
            sb.append(")");
        } else if (attributionZdbIDs.size() > 1) {
            /* todo: there should be some more infrastructure for the showpubs links */
            StringBuilder uri = new StringBuilder("?MIval=aa-showpubs.apg");
            uri.append("&orgOID=");
            uri.append(zdbId);
            uri.append("&rtype=marker&recattrsrctype=standard");
            uri.append("&OID=");
            String count = String.valueOf(attributionZdbIDs.size());

            sb.append(" (");
//            sb.append(EntityPresentation.getWebdriverLink(uri.toString(), zdbId, count));
            sb.append(EntityPresentation.getWebdriverLink(uri.toString(), markerRelationshipZdbId, count));
            sb.append(")");
        }

        return sb.toString();
    }

    public String getOrderThisLink() {

        String s = "";

        if (CollectionUtils.isNotEmpty(organizationLinks)) {
            for (OrganizationLink organizationLink : organizationLinks) {
                s += organizationLink.getOrderThisLink() + " ";
            }
        }

        return s;

//        if (sourceUrl != null) {
//            return "(<a href=\"" + sourceUrl + (supplierAccession == null ? "" : supplierAccession) + "\">" + sourceDisplayText + "</a>)";
//        } else if (supplierZdbId != null) {
//            return "(<a href=\"/" + ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT +
//                    "?MIval-aa-"
//                    + (supplierZdbId.contains("COMPANY") ? "companyview.apg" : "labview.apg")
//                    + "&OID=" + supplierZdbId + "\">order this</a>)";
//        } else {
//            return "";
//        }
    }

    public boolean isIs1To2() {
        return is1To2;
    }

    public void setIs1To2(boolean is1To2) {
        this.is1To2 = is1To2;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviationOrder() {
        return abbreviationOrder;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }

    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
    }

    public String getMarkerRelationshipZdbId() {
        return markerRelationshipZdbId;
    }

    public void setMarkerRelationshipZdbId(String markerRelationshipZdbId) {
        this.markerRelationshipZdbId = markerRelationshipZdbId;
    }

    public String getMarkerType() {
        return markerType;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public String getMarkerRelationshipAttributionPubZdbId() {
        return markerRelationshipAttributionPubZdbId;
    }

    public void setMarkerRelationshipAttributionPubZdbId(String markerRelationshipAttributionPubZdbId) {
        this.markerRelationshipAttributionPubZdbId = markerRelationshipAttributionPubZdbId;
    }

    public int getNumAttributions() {
        return numAttributions;
    }

    public void setNumAttributions(int numAttributions) {
        this.numAttributions = numAttributions;
    }

    public String getSupplierZdbId() {
        return supplierZdbId;
    }

    public void setSupplierZdbId(String supplierZdbId) {
        this.supplierZdbId = supplierZdbId;
    }

    @Override
    public String getLink() {
        if (markerType != null && markerType.toLowerCase().contains("gene")) {
            return "<i>" + link + "</i>";
        } else {
            return link;
        }
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAttributionZdbID() {
        return attributionZdbID;
    }

    public void setAttributionZdbID(String attributionZdbID) {
        this.attributionZdbID = attributionZdbID;
        this.attributionZdbIDs.add(attributionZdbID);
    }

    public Set<String> getAttributionZdbIDs() {
        return attributionZdbIDs;
    }

    public void setAttributionZdbIDs(Set<String> attributionZdbIDs) {
        this.attributionZdbIDs = attributionZdbIDs;
    }

    public void addAttributionLink(String attributionZdbID) {
        this.attributionZdbIDs.add(attributionZdbID);
    }

    public String getArbitraryOrder() {
        return arbitraryOrder;
    }

    public void setArbitraryOrder(int arbitraryOrder) {
        this.arbitraryOrder = String.valueOf(arbitraryOrder);
    }

    public void setArbitraryOrder(String arbitraryOrder) {
        this.arbitraryOrder = arbitraryOrder;
    }

    public String getMappedMarkerRelationshipType() {
        if (mappedMarkerRelationshipType == null) {
            return relationshipType;
        }
        return mappedMarkerRelationshipType;
    }

    public void setMappedMarkerRelationshipType(String mappedMarkerRelationshipType) {
        this.mappedMarkerRelationshipType = mappedMarkerRelationshipType;
    }

    public void addOrganizationLink(OrganizationLink organizationLink) {
        organizationLinks.add(organizationLink);
    }

    public Set<OrganizationLink> getOrganizationLinks() {
        return organizationLinks;
    }

    public void setOrganizationLinks(Set<OrganizationLink> organizationLinks) {
        this.organizationLinks = organizationLinks;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MarkerRelationshipPresentation");
        sb.append("{relationshipType='").append(relationshipType).append('\'');
        sb.append(", abbreviation='").append(abbreviation).append('\'');
        sb.append(", zdbId='").append(zdbId).append('\'');
        sb.append(", markerRelationshipZdbId='").append(markerRelationshipZdbId).append('\'');
        sb.append(", markerType='").append(markerType).append('\'');
        sb.append(", is1To2=").append(is1To2);
        sb.append(", mappedMarkerRelationshipType='").append(mappedMarkerRelationshipType).append('\'');
        sb.append(", arbitraryOrder='").append(arbitraryOrder).append('\'');
        sb.append(", organizationLinks='").append(organizationLinks.size()).append('\'');
        for (OrganizationLink organizationLink : organizationLinks) {
            sb.append(", organizationLink='").append(organizationLink).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }

    public void addOrganizationLinks(Set<OrganizationLink> organizationLinkList) {
        for (OrganizationLink organizationLink : organizationLinkList) {
            addOrganizationLink(organizationLink);
        }
    }
}
