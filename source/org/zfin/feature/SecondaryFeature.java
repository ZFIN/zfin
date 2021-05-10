package org.zfin.feature;

import org.zfin.marker.ReplacedData;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("Feature")
public class SecondaryFeature extends ReplacedData {

    @ManyToOne
    @JoinColumn(name = "zrepld_new_zdb_id")
    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}