package org.zfin.mapping;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;

/**
 * Genomic location info for a feature.
 */
@Entity
@DiscriminatorValue("Feat")
@Getter
@Setter
public class FeatureGenomeLocation extends GenomeLocation {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sfclg_data_zdb_id", insertable = false, updatable = false)
    private Feature feature;

    @Transient
    private String assemblyNum;

    public void setFeature(Feature feature) {
        this.feature = feature;
        this.entityID = feature.getZdbID();
    }
}
