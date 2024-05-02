package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;

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

}
