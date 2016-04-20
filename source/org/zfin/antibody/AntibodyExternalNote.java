package org.zfin.antibody;

import org.zfin.ExternalNote;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Antibody specific external note.
 */
@Entity
@DiscriminatorValue("antibody")
public class AntibodyExternalNote extends ExternalNote {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private Antibody antibody;

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

}
