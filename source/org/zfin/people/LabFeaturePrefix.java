package org.zfin.people;

import org.zfin.feature.FeaturePrefix;

import java.io.Serializable;


public class LabFeaturePrefix implements Serializable{

    private Lab lab ;
    private Boolean isCurrentDesignation;
    private FeaturePrefix featurePrefix ;


    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public Boolean getCurrentDesignation() {
        return isCurrentDesignation;
    }

    public void setCurrentDesignation(Boolean currentDesignation) {
        this.isCurrentDesignation = currentDesignation;
    }

    public FeaturePrefix getFeaturePrefix() {
        return featurePrefix;
    }

    public void setFeaturePrefix(FeaturePrefix featurePrefix) {
        this.featurePrefix = featurePrefix;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LabFeaturePrefix");
        sb.append("{lab=").append(lab);
        sb.append(", isCurrentDesignation=").append(isCurrentDesignation);
        sb.append(", featurePrefix=").append(featurePrefix);
        sb.append('}');
        return sb.toString();
    }
}
