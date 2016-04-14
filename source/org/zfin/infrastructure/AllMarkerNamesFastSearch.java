package org.zfin.infrastructure;

import org.zfin.marker.Marker;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Fast Search convenience class for markers.
 */
@Entity
@DiscriminatorValue("Marker")
public class AllMarkerNamesFastSearch extends AllNamesFastSearch {

    @ManyToOne
    @JoinColumn(name = "allmapnm_zdb_id")
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
