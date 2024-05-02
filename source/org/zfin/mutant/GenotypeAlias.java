package org.zfin.mutant;

import org.zfin.infrastructure.DataAlias;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
@DiscriminatorValue("Genoty")
public class GenotypeAlias extends DataAlias {

    @ManyToOne
    @JoinColumn(name = "dalias_data_zdb_id")
    private Genotype genotype;

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }
}
