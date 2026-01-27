package org.zfin.mapping;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;

/**
 * Genomic location info for a marker.
 */
@Entity
@DiscriminatorValue("Mark")
@Getter
@Setter
public class MarkerGenomeLocation extends GenomeLocation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sfclg_data_zdb_id", insertable = false, updatable = false)
    private Marker marker;

    public void setMarker(Marker marker) {
        this.marker = marker;
        this.entityID = marker.getZdbID();
    }
}
