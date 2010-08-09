package org.zfin.mutant;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;


/**
 * map as string for now
 */

public class FeatureMarkerRelationship implements Comparable {

    private String zdbID;
    private Type type;
    private Feature feature;
     private Marker marker;
     private Set<PublicationAttribution> publications;
    private FeatureMarkerRelationshipType featureMarkerRelationshipType;


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

      public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

       public enum Type {

        IS_ALLELE_OF("is allele of"),
    CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE("contains phenotypic sequence feature"),
     CONTAINS_INNOCUOUS_SEQUENCE_FEATURE("contains innocuous sequence feature"),
     MARKERS_MISSING("markers missing"),
           MARKERS_MOVED("markers moved"),
           MARKERS_PRESENT("markers present");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No MarkerRelationship type of string " + type + " found.");
        }
    }

    public FeatureMarkerRelationshipType getFeatureMarkerRelationshipType() {
        return featureMarkerRelationshipType;
    }

    public void setFeatureMarkerRelationshipType(FeatureMarkerRelationshipType featureMarkerRelationshipType) {
        this.featureMarkerRelationshipType = featureMarkerRelationshipType;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
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
                ", feature='" + feature + '\'' +
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
   public int compareTo(Object anotherMarkerRelationship) {
        return marker.compareTo(((FeatureMarkerRelationship) anotherMarkerRelationship).getMarker());
    }
}

