package org.zfin.marker.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.publication.presentation.PublicationPresentation;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class LinkDisplay implements ProvidesLink{

    private String referenceDatabaseName ;
    private String accession ;
    private String urlPrefix ;
    private Set<String> attributionZdbIDs = new HashSet<String>();
    private String markerZdbID;
    private String urlSuffix;
    private Integer significance ;
    private String dblinkZdbID ;

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

        if (attributionZdbIDs.size()==1) {
            sb.append(" (");
            sb.append(PublicationPresentation.getLink(attributionZdbIDs.iterator().next(), "1"));
            sb.append(")");
        }
        else
        if (attributionZdbIDs.size() > 1) {
            /* todo: there should be some more infrastructure for the showpubs links */
            StringBuilder uri = new StringBuilder("?MIval=aa-showpubs.apg");
            uri.append("&orgOID=");
            uri.append(markerZdbID);
            uri.append("&rtype=marker&recattrsrctype=standard");
            uri.append("&OID=");
            String count = String.valueOf(attributionZdbIDs.size());

            sb.append(" (");
            sb.append(EntityPresentation.getWebdriverLink(uri.toString(), dblinkZdbID, count));
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

    public Set<String> getAttributionZdbIDs() {
        return attributionZdbIDs;
    }

    public void setAttributionZdbIDs(Set<String> attributionZdbIDs) {
        this.attributionZdbIDs = attributionZdbIDs ;
    }

    public int getNumPublications() {
        if(attributionZdbIDs !=null){
            return attributionZdbIDs.size();
        }
        return 0 ;
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

    public void addAttributionZdbIDs(Set<String> publicationZdbIDs) {
        this.attributionZdbIDs.addAll(publicationZdbIDs);
    }

    public void addAttributionZdbID(String publicationZdbID) {
        this.attributionZdbIDs.add(publicationZdbID);
    }

    public String getDblinkZdbID() {
        return dblinkZdbID;
    }

    public void setDblinkZdbID(String dblinkZdbID) {
        this.dblinkZdbID = dblinkZdbID;
    }
}
