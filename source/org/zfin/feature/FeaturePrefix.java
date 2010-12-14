package org.zfin.feature;


public class FeaturePrefix {

    private int featurePkID;
    private String prefixString;
    // this is a convenience method that says, is this the active prefix of a given set of prefixes
    private boolean activeForSet ;
    private String institute;

    public int getFeaturePkID() {
        return featurePkID;
    }

    public void setFeaturePkID(int featurePkID) {
        this.featurePkID = featurePkID;
    }

    public String getPrefixString() {
        return prefixString;
    }

    public void setPrefixString(String prefixString) {
        this.prefixString = prefixString;
    }

    public boolean isActiveForSet() {
        return activeForSet;
    }

    public void setCurrentDesignationForSet(boolean activeForSet) {
        this.activeForSet = activeForSet;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FeaturePrefix");
        sb.append("{prefixString='").append(prefixString).append('\'');
        sb.append(", institute='").append(institute).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
