package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;
import org.zfin.feature.Variant;

/**
 * Feature Location .
 */
@Setter
@Getter
@Entity
@DiscriminatorValue("Feat")
public class FeatureLocation extends Location {

    @ManyToOne
    @JoinColumn(name = "sfcl_feature_zdb_id")
    private Feature feature;

    /** The variant this location places (ZFIN-10622); null for a feature-level location. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sfcl_variant_zdb_id")
    private Variant variant;

    public boolean containsLocationData() {
        return this.getStartLocation() != null && !this.getStartLocation().toString().isEmpty()
                && this.getEndLocation() != null && !this.getEndLocation().toString().isEmpty()
                && this.getAssembly() != null;
    }

    public boolean emptyLocationData() {
        return !containsLocationData();
    }
}
