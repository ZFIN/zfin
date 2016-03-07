package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;

import javax.persistence.*;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
@Entity
@Table(name = "PHENOTYPE_GENERATED_CURATED_MAPPING")
public class PhenotypeCurationSearch {


    @Id
    @ManyToOne()
    @JoinColumn(name = "pgcm_pg_id")
    private PhenotypeWarehouse phenoWarehouse;

    @Id
    @Column(name = "pgcm_source_id")
    private String phenoOrExpID;

    public PhenotypeWarehouse getPhenoWarehouse() {
        return phenoWarehouse;
    }

    public void setPhenoWarehouse(PhenotypeWarehouse phenoWarehouse) {
        this.phenoWarehouse = phenoWarehouse;
    }

    public String getPhenoOrExpID() {
        return phenoOrExpID;
    }

    public void setPhenoOrExpID(String phenoOrExpID) {
        this.phenoOrExpID = phenoOrExpID;
    }
}


