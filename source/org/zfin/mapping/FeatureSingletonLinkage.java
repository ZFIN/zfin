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

@Entity
@DiscriminatorValue("Feature")
@Getter
@Setter
public class FeatureSingletonLinkage extends SingletonLinkage {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lsingle_member_zdb_id")
    private Feature feature;

    @PostLoad
    private void initTransientFields() {
        entity = feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
        entity = feature;
    }
}
