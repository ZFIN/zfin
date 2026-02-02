package org.zfin.mapping;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;

@Entity
@DiscriminatorValue("Mark")
@Getter
@Setter
public class MappedMarkerImpl extends MappedMarker {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marker_id", insertable = false, updatable = false)
    private Marker marker;

    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        } else if (!(o instanceof MappedMarkerImpl)) {
            return o.toString().compareTo(toString());
        }
        // both MappedMarker
        else {
            MappedMarkerImpl mappedMarker = (MappedMarkerImpl) o;
            if (!lg.equalsIgnoreCase(mappedMarker.getLg())) {
                return lg.toLowerCase().compareTo(mappedMarker.getLg().toLowerCase());
            } else {
                return marker.compareTo(mappedMarker.getMarker());
            }
        }
    }

    @Override
    public String getEntityID() {
        return marker.getZdbID();
    }

    @Override
    public String getEntityAbbreviation() {
        return marker.getAbbreviation();
    }
}
