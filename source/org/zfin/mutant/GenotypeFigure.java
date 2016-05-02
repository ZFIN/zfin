package org.zfin.mutant;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "GENOTYPE_FIGURE_FAST_SEARCH")
public class GenotypeFigure implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gffs_serial_id")
    private int id;
    @ManyToOne()
    @JoinColumn(name = "gffs_geno_zdb_id")
    private Genotype genotype;
    @ManyToOne()
    @JoinColumn(name = "gffs_fish_zdb_id")
    private Fish fish;
    @ManyToOne()
    @JoinColumn(name = "gffs_fig_zdb_id")
    private Figure figure;
    @ManyToOne()
    @JoinColumn(name = "gffs_morph_zdb_id")
    private Marker sequenceTargetingReagent;
    @ManyToOne()
    @JoinColumn(name = "gffs_pg_id")
    private PhenotypeWarehouse phenotypeWarehouse;
    @ManyToOne()
    @JoinColumn(name = "gffs_psg_id")
    private PhenotypeStatementWarehouse phenotypeStatement;
    @ManyToOne()
    @JoinColumn(name = "gffs_genox_zdb_id")
    private FishExperiment fishExperiment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Marker getSequenceTargetingReagent() {
        return sequenceTargetingReagent;
    }

    public void setSequenceTargetingReagent(Marker sequenceTargetingReagent) {
        this.sequenceTargetingReagent = sequenceTargetingReagent;
    }

    public PhenotypeWarehouse getPhenotypeWarehouse() {
        return phenotypeWarehouse;
    }

    public void setPhenotypeWarehouse(PhenotypeWarehouse phenotypeWarehouse) {
        this.phenotypeWarehouse = phenotypeWarehouse;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public PhenotypeStatementWarehouse getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public void setPhenotypeStatement(PhenotypeStatementWarehouse phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }
}
