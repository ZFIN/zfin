package org.zfin.marker.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.publication.presentation.PublicationPresentation;

/**
 */
public class LinkDisplay implements ProvidesLink{

    private String referenceDatabaseName ;
    private String accession ;
    private String urlPrefix ;
    private String publicationZdbID;
    private int numPublications ;
    private String markerZdbID;
    private String urlSuffix;
    private Integer significance ;

    public String getDisplayName(){
        return referenceDatabaseName + ":" + accession ;
    }

    @Override
    public String getLink(){
        return urlPrefix + accession + (urlSuffix!=null ? urlSuffix  : "") ;
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink() + " " + getAttributionLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLinkWithAttribution();
    }

    public String getAttributionLink(){
        StringBuilder sb = new StringBuilder("");

        if (numPublications == 1 || publicationZdbID!=null) {
            sb.append(" (");
            sb.append(PublicationPresentation.getLink(publicationZdbID, "1"));
            sb.append(")");
        }
        else
        if (numPublications > 1) {
            /* todo: there should be some more infrastructure for the showpubs links */
            StringBuilder uri = new StringBuilder("?MIval=aa-showpubs.apg");
            uri.append("&orgOID=");
            uri.append(markerZdbID);
            uri.append("&rtype=marker&recattrsrctype=standard");
            uri.append("&OID=");
            String count = String.valueOf(numPublications);

            sb.append(" (");
            sb.append(EntityPresentation.getWebdriverLink(uri.toString(), markerZdbID, count));
            sb.append(")");
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

    public String getPublicationZdbID() {
        return publicationZdbID;
    }

    public void setPublicationZdbID(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID;
    }

    public int getNumPublications() {
        return numPublications;
    }

    public void setNumPublications(int numPublications) {
        this.numPublications = numPublications;
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
}
