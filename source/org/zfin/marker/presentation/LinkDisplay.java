package org.zfin.marker.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.publication.presentation.PublicationPresentation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class LinkDisplay implements ProvidesLink {

    private String referenceDatabaseName;
    private String referenceDatabaseZdbID;
    private String accession;
    private String urlPrefix;
    private Set<MarkerReferenceBean> references;
    private String markerZdbID;
    private String urlSuffix;
    private Integer significance;
    private String dblinkZdbID;
    private String length;
    private String dataType;
    private Integer typeOrder;

    public String getDisplayName() {
        return referenceDatabaseName + ":" + accession;
    }

    @Override
    public String getLink() {
        return urlPrefix + accession + (urlSuffix != null ? urlSuffix : "");
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink() + " " + getAttributionLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLinkWithAttribution();
    }

    public String getAttributionLink() {
        StringBuilder sb = new StringBuilder("");

        if (references == null) {
            return "";
        }
        else if (references.size() == 1) {
            sb.append(" (");
            sb.append(PublicationPresentation.getLink(references.iterator().next().getZdbID(), "1"));
            sb.append(")");
        } else if (references.size() > 1) {
            String count = String.valueOf(references.size());

            sb.append(" (<a href=\"/action/infrastructure/data-citation-list/");
            sb.append(dblinkZdbID);
            sb.append("\">");
            sb.append(count);
            sb.append("</a>)");
        }

        return sb.toString();
    }


    public String getReferenceDatabaseName() {
        return referenceDatabaseName;
    }

    public void setReferenceDatabaseName(String referenceDatabaseName) {
        this.referenceDatabaseName = referenceDatabaseName;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public Set<MarkerReferenceBean> getReferences() {
        return references;
    }

    public void setReferences(Set<MarkerReferenceBean> references) {
        this.references = references;
    }

    public void addReference(MarkerReferenceBean reference) {
        if (references == null) {
            references = new HashSet<>();
        }
        references.add(reference);
    }

    public void addReferences(Collection<MarkerReferenceBean> references) {
        if (this.references == null) {
            this.references = new HashSet<>();
        }
        this.references.addAll(references);
    }

    public String getMarkerZdbID() {
        return markerZdbID;
    }

    public void setMarkerZdbID(String markerZdbID) {
        this.markerZdbID = markerZdbID;
    }

    public String getUrlSuffix() {
        return urlSuffix;
    }

    public void setUrlSuffix(String urlSuffix) {
        this.urlSuffix = urlSuffix;
    }

    public Integer getSignificance() {
        return significance;
    }

    public void setSignificance(Integer significance) {
        this.significance = significance;
    }

    public String getDblinkZdbID() {
        return dblinkZdbID;
    }

    public void setDblinkZdbID(String dblinkZdbID) {
        this.dblinkZdbID = dblinkZdbID;
    }

    public String getReferenceDatabaseZdbID() {
        return referenceDatabaseZdbID;
    }

    public void setReferenceDatabaseZdbID(String referenceDatabaseZdbID) {
        this.referenceDatabaseZdbID = referenceDatabaseZdbID;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getTypeOrder() {
        return typeOrder;
    }

    public void setTypeOrder(Integer typeOrder) {
        this.typeOrder = typeOrder;
    }
}
