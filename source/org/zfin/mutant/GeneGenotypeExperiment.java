package org.zfin.mutant;

import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "mutant_fast_search")
@IdClass(GeneGenotypeExperimentId.class)
public class GeneGenotypeExperiment implements Serializable {

    @Id
    @Column(name = "mfs_data_zdb_id")
    private String markerId;

    @Id
    @Column(name = "mfs_genox_zdb_id")
    private String fishExperimentId;

    @ManyToOne
    @JoinColumn(name = "mfs_data_zdb_id")
    private Marker gene;

    @ManyToOne
    @JoinColumn(name = "mfs_genox_zdb_id")
    private FishExperiment fishExperiment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneGenotypeExperiment that = (GeneGenotypeExperiment) o;
        return Objects.equals(gene, that.gene) &&
                Objects.equals(fishExperiment, that.fishExperiment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gene, fishExperiment);
    }
}
