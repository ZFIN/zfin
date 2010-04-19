package org.zfin.gbrowse.repository;

import org.zfin.gbrowse.GBrowseFeature;
import org.zfin.marker.Transcript;
import org.zfin.marker.Marker;

import java.util.Set;

public interface GBrowseRepository {
    public Set<GBrowseFeature> getGBrowseFeaturesForMarker(Marker marker);
    public Boolean isMarkerInGBrowse(Marker marker);
}
