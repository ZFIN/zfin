package org.zfin.feature;

import javax.persistence.*;

@Entity
@Table(name = "feature_tracking")
public class FeatureTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ft_pk_id")
    private int pkid;
    @ManyToOne
    @JoinColumn(name = "ft_feature_zdb_id")
    private Feature feature;
    @Column(name = "ft_feature_abbrev")
    private String featTrackingFeatAbbrev;

    public int getPkid() {
        return pkid;
    }

    public void setPkid(int pkid) {
        this.pkid = pkid;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public String getFeatTrackingFeatAbbrev() {
        return featTrackingFeatAbbrev;
    }

    public void setFeatTrackingFeatAbbrev(String featTrackingFeatAbbrev) {
        this.featTrackingFeatAbbrev = featTrackingFeatAbbrev;
    }

}
