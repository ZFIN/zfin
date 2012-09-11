package org.zfin.feature;

/**ture
 */
public class FeatureTracking {

    private int pkid;

    public int getPkid() {
        return pkid;
    }

    public void setPkid(int pkid) {
        this.pkid = pkid;
    }

    public String getFeatTrackingFeatZdbID() {
        return featTrackingFeatZdbID;
    }

    public void setFeatTrackingFeatZdbID(String featTrackingFeatZdbID) {
        this.featTrackingFeatZdbID = featTrackingFeatZdbID;
    }

    public String getFeatTrackingFeatAbbrev() {
        return featTrackingFeatAbbrev;
    }

    public void setFeatTrackingFeatAbbrev(String featTrackingFeatAbbrev) {
        this.featTrackingFeatAbbrev = featTrackingFeatAbbrev;
    }

    private String featTrackingFeatZdbID;
    private String featTrackingFeatAbbrev;



}
