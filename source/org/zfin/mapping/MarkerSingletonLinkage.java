package org.zfin.mapping;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;

@Entity
@DiscriminatorValue("Marker ")
@Getter
@Setter
public class MarkerSingletonLinkage extends SingletonLinkage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lsingle_member_zdb_id")
    private Marker marker;

    @PostLoad
    private void initTransientFields() {
        entity = marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        entity = marker;
    }
}
