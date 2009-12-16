package org.zfin.mapping.repository;

import org.zfin.marker.Marker;

import java.util.List;
import java.util.TreeSet;

public interface LinkageRepository {

    List<String> getDirectMappedMarkers(Marker marker);

    TreeSet<String> getLG(Marker marker);
}
