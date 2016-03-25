package org.zfin.marker;

import org.zfin.ExternalNote;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Note entered by Curators concerning the existence or absence of orthology.
 */
@Entity
@DiscriminatorValue("orthology")
public class OrthologyNote extends ExternalNote {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

}