package org.zfin.feature;

import org.zfin.marker.ReplacedData;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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