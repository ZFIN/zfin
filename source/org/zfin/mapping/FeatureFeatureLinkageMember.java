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

@Entity
@DiscriminatorValue("FeatFeat")
@Getter
@Setter
public class FeatureFeatureLinkageMember extends LinkageMember {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_1_zdb_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lms_member_2_zdb_id")
    private Feature pairedFeature;

    @PostLoad
    private void initTransientFields() {
        entityOne = feature;
        entityTwo = pairedFeature;
    }

    public void setPairedFeature(Feature pairedFeature) {
        this.pairedFeature = pairedFeature;
        entityOne = feature;
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
