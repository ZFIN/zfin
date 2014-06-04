package org.zfin.mapping;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;

/**
 * Created by cmpich on 3/4/14.
 */
public class MarkerFeatureLinkageMember extends LinkageMember {

    private Marker marker;
    private Feature feature;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        entityOne = marker;
        entityTwo = feature;
    }

    @Override
    public ZdbID getLinkedMember() {
        return feature;
    }

    @Override
    public LinkageMember getInverseMember() {
        FeatureMarkerLinkageMember inverse = new FeatureMarkerLinkageMember();
        inverse.setMarker(marker);
        inverse.setFeature(feature);
        inverse.setDistance(distance);
        inverse.setMetric(metric);
        inverse.setLinkage(linkage);
        inverse.setLod(lod);
        return inverse;
    }

    @Override
    public int compareTo(LinkageMember o) {
        return 0;
    }
}
