package org.zfin.mapping;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;

@Entity
@DiscriminatorValue("MarkFeat")
@Getter
@Setter
public class MarkerFeatureLinkageMember extends LinkageMember {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_1_zdb_id")
    private Marker marker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_2_zdb_id")
    private Feature feature;

    @PostLoad
    private void initTransientFields() {
        entityOne = marker;
        entityTwo = feature;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        entityOne = marker;
        entityTwo = feature;
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
}
