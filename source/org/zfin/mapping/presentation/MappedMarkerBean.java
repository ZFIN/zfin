package org.zfin.mapping.presentation;

import org.zfin.mapping.MappedMarker;
import org.zfin.marker.Marker;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;

public class MappedMarkerBean {

    private Logger logger = Logger.getLogger(MappedMarkerBean.class) ;

    private boolean hasMappedMarkers = false ;
    private Marker marker ;


    private List<String> unMappedMarkers ;

    public boolean isHasMappedMarkers() {
        return hasMappedMarkers;
    }

    public void setHasMappedMarkers(boolean hasMappedMarkers) {
        this.hasMappedMarkers = hasMappedMarkers;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public List<String> getUnMappedMarkers() {
        return unMappedMarkers;
    }

    public void setUnMappedMarkers(List<String> unMappedMarkers) {
        this.unMappedMarkers = unMappedMarkers;
    }
}
