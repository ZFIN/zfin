package org.zfin.fish;

import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class FeatureGroup {

    private long ID;
    private String genotypeID;
    private Set<FeatureGroupMember> featureGroupMembers;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getGenotypeID() {
        return genotypeID;
    }

    public void setGenotypeID(String genotypeID) {
        this.genotypeID = genotypeID;
    }

    public Set<FeatureGroupMember> getFeatureGroupMembers() {
        return featureGroupMembers;
    }

    public void setFeatureGroupMembers(Set<FeatureGroupMember> featureGroupMembers) {
        this.featureGroupMembers = featureGroupMembers;
    }
}
