package org.zfin.mapping;

import org.zfin.feature.Feature;
import org.zfin.marker.Marker;

import java.util.Set;


public class Linkage {
    private String zdbID;
    private String lg;
    private Set<Marker> linkageMemberMarkers;
    private Set<Feature> linkageMemberFeatures;    

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getLg() {
        return lg;
    }

    public void setLg(String lg) {
        this.lg = lg;
    }

    public Set<Marker> getLinkageMemberMarkers() {
        return linkageMemberMarkers;
    }

    public void setLinkageMemberMarkers(Set<Marker> linkageMemberMarkers) {
        this.linkageMemberMarkers = linkageMemberMarkers;
    }

    public Set<Feature> getLinkageMemberFeatures() {
        return linkageMemberFeatures;
    }

    public void setLinkageMemberFeatures(Set<Feature> linkageMemberFeatures) {
        this.linkageMemberFeatures = linkageMemberFeatures;
    }
}
