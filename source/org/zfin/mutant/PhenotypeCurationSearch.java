package org.zfin.mutant;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Main Experiment object that contains expression annotations.
 */
@Entity
@Table(name = "phenotype_generated_curated_mapping")
public class PhenotypeCurationSearch implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhenotypeCurationSearch that = (PhenotypeCurationSearch) o;

        if (phenoWarehouse.getId() != that.phenoWarehouse.getId()) return false;
        return phenoOrExpID.equals(that.phenoOrExpID);

    }

    @Override
    public int hashCode() {
        int result = phenoWarehouse != null ? phenoWarehouse.hashCode() : 0;
        result = 31 * result + (phenoOrExpID != null ? phenoOrExpID.hashCode() : 0);
        return result;
    }
}


