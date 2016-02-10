package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionPhenotypeTerm;
import org.zfin.expression.Figure;
import org.zfin.mutant.Genotype;

import javax.persistence.*;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
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

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        this.start = start;
    }

    public Set<PhenotypeStatementWarehouse> getStatementWarehouseSet() {
        return statementWarehouseSet;
    }

    public void setStatementWarehouseSet(Set<PhenotypeStatementWarehouse> statementWarehouseSet) {
        this.statementWarehouseSet = statementWarehouseSet;
    }
}

