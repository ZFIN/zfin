package org.zfin.fish;

import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FeatureGroupMember {

    private long ID;
    private String featureID;
    private String featureName;
    private FeatureGroup featureGroup;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getFeatureID() {
        return featureID;
    }

    public void setFeatureID(String featureID) {
        this.featureID = featureID;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public FeatureGroup getFeatureGroup() {
        return featureGroup;
    }

    public void setFeatureGroup(FeatureGroup featureGroup) {
        this.featureGroup = featureGroup;
    }
}
