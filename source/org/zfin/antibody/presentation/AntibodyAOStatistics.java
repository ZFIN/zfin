package org.zfin.antibody.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.AnatomyFact;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Class that maps to a statistics table for antibodies
 */
@Entity
@DiscriminatorValue("Antibody")
public class AntibodyAOStatistics extends AnatomyFact {

    @ManyToOne()
    @JoinColumn(name = "fstat_feat_zdb_id", insertable = false, updatable = false)
    Antibody antibody;

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }
}
