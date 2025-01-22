package org.zfin.feature;

import org.zfin.ExternalNote;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Note entered by Curators concerning the existence or absence of orthology.
 */
@Entity
@DiscriminatorValue("feature")
public class FeatureNote extends ExternalNote {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public boolean isVariantNote() {
        return tag.contains("variant");
    }

}