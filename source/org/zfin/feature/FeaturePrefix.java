package org.zfin.feature;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.zfin.infrastructure.EntityZdbID;

@Setter
@Getter
@Entity
@Table(name = "feature_prefix")
public class FeaturePrefix implements EntityZdbID {

    public static final String ZF = "zf";

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

    public void setCurrentDesignationForSet(boolean activeForSet) {
        this.activeForSet = activeForSet;
    }

    @Override
    public String toString() {
        return "FeaturePrefix" +
                "{prefixString='" + prefixString + "'" +
                ", institute='" + institute + "'" +
                '}';
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
    public void setZdbID(String zdbID) {}
}
