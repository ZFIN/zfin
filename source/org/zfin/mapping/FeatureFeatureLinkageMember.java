package org.zfin.mapping;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;

/**
 * Created by cmpich on 3/4/14.
 */
public class FeatureFeatureLinkageMember extends LinkageMember {

    private Feature feature;
    private Feature pairedFeature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getPairedFeature() {
        return pairedFeature;
    }

    public void setPairedFeature(Feature pairedFeature) {
        this.pairedFeature = pairedFeature;
        entityOne = pairedFeature;
        entityTwo = pairedFeature;
    }

    @Override
    public ZdbID getLinkedMember() {
        return pairedFeature;
    }

    @Override
    public LinkageMember getInverseMember() {
        FeatureFeatureLinkageMember inverse;
        try {
            inverse = (FeatureFeatureLinkageMember) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
        inverse.setFeature(pairedFeature);
        inverse.setPairedFeature(feature);
        return inverse;
    }
}
