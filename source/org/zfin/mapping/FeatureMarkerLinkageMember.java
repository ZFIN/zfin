package org.zfin.mapping;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;

/**
 *
 */
public class FeatureMarkerLinkageMember extends LinkageMember {

    private Feature feature;
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        entityOne = feature;
        entityTwo = marker;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    @Override
    public ZdbID getLinkedMember() {
        return marker;
    }

    @Override
    public LinkageMember getInverseMember() {
        MarkerFeatureLinkageMember inverse = new MarkerFeatureLinkageMember();
        inverse.setMarker(marker);
        inverse.setFeature(feature);
        inverse.setDistance(distance);
        inverse.setMetric(metric);
        inverse.setLinkage(linkage);
        inverse.setLod(lod);

        return inverse;
    }
}
