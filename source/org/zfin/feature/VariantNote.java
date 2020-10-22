package org.zfin.feature;

import org.zfin.ExternalNote;

import javax.persistence.*;

/**
 * Note entered by Curators concerning the existence or absence of orthology.
 */
@Entity
@DiscriminatorValue("variant")
public class VariantNote extends ExternalNote {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private FeatureGenomicMutationDetail featureGenomicMutationDetail;

    public FeatureGenomicMutationDetail getFeatureGenomicMutationDetail() {
        return featureGenomicMutationDetail;
    }

    public void setFeatureGenomicMutationDetail(FeatureGenomicMutationDetail featureGenomicMutationDetail) {
        this.featureGenomicMutationDetail = featureGenomicMutationDetail;
    }

}