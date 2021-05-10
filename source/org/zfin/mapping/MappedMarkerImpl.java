package org.zfin.mapping;

import org.zfin.marker.Marker;

/**
 * Created by cmpich on 3/25/14.
 */
public class MappedMarkerImpl extends MappedMarker {

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        } else if (false == (o instanceof MappedMarkerImpl)) {
            return o.toString().compareTo(toString());
        }
        // both MappedMarker
        else {
            MappedMarkerImpl mappedMarker = (MappedMarkerImpl) o;
            if (false == lg.equalsIgnoreCase(mappedMarker.getLg())) {
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
