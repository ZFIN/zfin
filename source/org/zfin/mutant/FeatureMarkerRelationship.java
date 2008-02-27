package org.zfin.mutant;

import org.zfin.marker.Marker;


/**

 * map as string for now

 */

public class FeatureMarkerRelationship {

    private String zdbID;
    private String type;
    private String featureZdbId;
    private String markerZdbId;
    private Marker marker;
    private Feature feature;



    public static final String IS_ALLELE_OF = "is allele of";

    public static final String CONTAINS_SEQUENCE_FEATURE = "contains sequence feature";



    public String getZdbID() {

        return zdbID;

    }



    public void setZdbID(String zdbID) {

        this.zdbID = zdbID;

    }



    public String getFeatureZdbId() {

        return featureZdbId;

    }



    public void setFeatureZdbId(String featureZdbId) {

        this.featureZdbId = featureZdbId;

    }



    public String getMarkerZdbId() {

        return markerZdbId;

    }



    public void setMarkerZdbId(String markerZdbId) {

        this.markerZdbId = markerZdbId;

    }



    public String getType() {

        return type;

    }



    public void setType(String type) {

        this.type = type;

    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}

