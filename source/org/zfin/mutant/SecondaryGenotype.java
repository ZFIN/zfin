package org.zfin.mutant;
import org.zfin.marker.ReplacedData;
import javax.persistence.*;

@Entity
@DiscriminatorValue("Genotype")
public class SecondaryGenotype extends ReplacedData {

    @ManyToOne
    @JoinColumn(name = "zrepld_new_zdb_id")
    private Genotype genotype;

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }
}