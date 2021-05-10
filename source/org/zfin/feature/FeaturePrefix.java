package org.zfin.feature;


import org.zfin.infrastructure.EntityZdbID;

import javax.persistence.*;

@Entity
@Table(name = "feature_prefix")
public class FeaturePrefix implements EntityZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fp_pk_id")
    private int featurePkID;
    @Column(name = "fp_prefix")
    private String prefixString;
    // this is a convenience method that says, is this the active prefix of a given set of prefixes
    @Transient
    private boolean activeForSet;
    @Column(name = "fp_institute_display")
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

    @Override
    public String getAbbreviation() {
        return prefixString;
    }

    @Override
    public String getAbbreviationOrder() {
        return prefixString;
    }

    @Override
    public String getEntityType() {
        return "Feature Prefix";
    }

    @Override
    public String getEntityName() {
        return prefixString;
    }

    @Override
    public String getZdbID() {
        return prefixString;
    }

    @Override
    public void setZdbID(String zdbID) {

    }
}
