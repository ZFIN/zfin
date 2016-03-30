package org.zfin.feature;

import org.zfin.ExternalNote;
import org.zfin.marker.Marker;

import javax.persistence.*;

/**
 * Note entered by Curators concerning the existence or absence of orthology.
 */
@Entity
@DiscriminatorValue("feature")
public class FeatureNote extends ExternalNote {

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "extnote_data_zdb_id")
    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }
}