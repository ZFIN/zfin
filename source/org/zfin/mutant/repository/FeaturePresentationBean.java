package org.zfin.mutant.repository;

/**
 *  This bean is used for display purposes only
 */
public class FeaturePresentationBean {
    private String featureZdbId ;
    private String abbrevation ;

    public String getFeatureZdbId() {
        return featureZdbId;
    }

    public void setFeatureZdbId(String featureZdbId) {
        this.featureZdbId = featureZdbId;
    }

    public String getAbbrevation() {
        return abbrevation;
    }

    public void setAbbrevation(String abbrevation) {
        this.abbrevation = abbrevation;
    }
}
