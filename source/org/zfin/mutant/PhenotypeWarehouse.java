package org.zfin.mutant;

import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
@Setter
@Getter
@Entity
@Table(name = "PHENOTYPE_SOURCE_GENERATED")
public class PhenotypeWarehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pg_id")
    private long id;
    @ManyToOne()
    @JoinColumn(name = "pg_genox_zdb_id")
    private FishExperiment fishExperiment;
    @ManyToOne()
    @JoinColumn(name = "pg_fig_zdb_id")
    private Figure figure;
    @ManyToOne()
    @JoinColumn(name = "pg_start_stg_zdb_id")
    private DevelopmentStage start;
    @ManyToOne()
    @JoinColumn(name = "pg_end_stg_zdb_id")
    private DevelopmentStage end;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "phenotypeWarehouse", orphanRemoval = true)
    private Set<PhenotypeStatementWarehouse> statementWarehouseSet;

}

