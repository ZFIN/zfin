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
@DiscriminatorValue("FeatMark")
@Getter
@Setter
public class FeatureMarkerLinkageMember extends LinkageMember {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_1_zdb_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_2_zdb_id")
    private Marker marker;

    @PostLoad
    private void initTransientFields() {
        entityOne = feature;
        entityTwo = marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
        entityOne = feature;
        entityTwo = marker;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        entityOne = feature;
        entityTwo = marker;
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
