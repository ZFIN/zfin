package org.zfin.feature;

import org.zfin.marker.MarkerTypeGroup;


public class FeatureMarkerRelationshipType {

    private String name;
    private FeatureTypeGroup FeatureTypeGroup;
    private MarkerTypeGroup MarkerTypeGroup;
    private String firstToSecondLabel;
    private String secondToFirstLabel;
    private boolean affectedMarkerFlag;

    public boolean isAffectedMarkerFlag() {
        return affectedMarkerFlag;
    }

    public void setAffectedMarkerFlag(boolean affectedMarkerFlag) {
        this.affectedMarkerFlag = affectedMarkerFlag;
    }

    public String getName() {
        return name;
    }

    public FeatureTypeGroup getFeatureTypeGroup() {
        return FeatureTypeGroup;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setFeatureTypeGroup(FeatureTypeGroup featureTypeGroup) {
        FeatureTypeGroup = featureTypeGroup;
    }

    public MarkerTypeGroup getMarkerTypeGroup() {
        return MarkerTypeGroup;
    }

    public void setMarkerTypeGroup(MarkerTypeGroup markerTypeGroup) {
        MarkerTypeGroup = markerTypeGroup;
    }

    public String getFirstToSecondLabel() {
        return firstToSecondLabel;
    }

    public void setFirstToSecondLabel(String firstToSecondLabel) {
        this.firstToSecondLabel = firstToSecondLabel;
    }

    public String getSecondToFirstLabel() {
        return secondToFirstLabel;
    }

    public void setSecondToFirstLabel(String secondToFirstLabel) {
        this.secondToFirstLabel = secondToFirstLabel;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureMarkerRelationshipType that = (FeatureMarkerRelationshipType) o;

        return false == (name != null ? false == name.equals(that.name) : that.name != null);

    }

}




