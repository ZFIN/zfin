package org.zfin.mutant;

import org.zfin.ExternalNote;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Genotype specific external note.
 */
@Entity
@DiscriminatorValue("genotype")
public class GenotypeExternalNote extends ExternalNote {

    @ManyToOne
    @JoinColumn(name = "extnote_data_zdb_id")
    private Genotype genotype;

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

}