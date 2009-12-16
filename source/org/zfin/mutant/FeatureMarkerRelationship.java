package org.zfin.mutant;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;


/**
 * map as string for now
 */

public class FeatureMarkerRelationship {

    private String zdbID;
    private String type;
    private String featureZdbId;
    private Marker marker;
    private Set<PublicationAttribution> publications;


    public static final String IS_ALLELE_OF = "is allele of";
    public static final String CONTAINS_SEQUENCE_FEATURE = "contains phenotypic sequence feature";
    public static final String CONTAINS_INNOCSEQUENCE_FEATURE = "contains innocuous sequence feature";


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFeatureZdbId() {
        return featureZdbId;
    }

    public void setFeatureZdbId(String featureZdbId) {
        this.featureZdbId = featureZdbId;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public String toString() {
        return "FeatureMarkerRelationship{" +
                "zdbID='" + zdbID + '\'' +
                ", type='" + type + '\'' +
                ", featureZdbId='" + featureZdbId + '\'' +
                ", marker=" + marker +
//                ", feature=" + feature +
                '}';
    }

    public Set<PublicationAttribution> getPublications() {
        if (publications == null)
            return new HashSet<PublicationAttribution>();
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public int getPublicationCount() {
        if (publications == null)
            return 0;
        else
            return publications.size();
    }

    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            for (PublicationAttribution pubAttr : getPublications())
                return pubAttr.getPublication();
        }
        return null;
    }
}

