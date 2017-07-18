package org.zfin.marker;

import javax.persistence.*;

@Entity
@DiscriminatorValue("Marker")
public class SecondaryMarker extends ReplacedData {

    @ManyToOne
    @JoinColumn(name = "zrepld_new_zdb_id")
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
