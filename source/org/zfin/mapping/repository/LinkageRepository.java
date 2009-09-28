package org.zfin.mapping.repository;

import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.zfin.marker.Clone;

import java.util.TreeSet;
import java.util.List;

public interface LinkageRepository {

    List<String> getDirectMappedMarkers(Marker marker) ;
    TreeSet<String> getLG(Marker marker);
}
