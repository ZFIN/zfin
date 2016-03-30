package org.zfin.feature;

import org.zfin.ExternalNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Comparator;

/**
 * Note entered by Curators concerning the existence or absence of orthology.
 */
@Entity
@DiscriminatorValue("feature")
public class FeatureNote extends ExternalNote implements Comparable<FeatureNote> {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public int compareTo(FeatureNote note) {
        return getZdbID().compareTo(note.getZdbID());
    }


}