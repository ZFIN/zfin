package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;

import java.util.Date;
import java.util.Set;

/**
 * Business object that describes a phenotype experiment, i.e. an experiment
 * that has phenotypic data annotated to (phenotype statement).
 *
 */
@Getter
@Setter
@Entity
@Table(name = "phenotype_experiment")
public class PhenotypeExperiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phenox_pk_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "phenox_genox_zdb_id")
    private FishExperiment fishExperiment;

    @ManyToOne
    @JoinColumn(name = "phenox_start_stg_zdb_id")
    private DevelopmentStage startStage;

    @ManyToOne
    @JoinColumn(name = "phenox_end_stg_zdb_id")
    private DevelopmentStage endStage;

    @ManyToOne
    @JoinColumn(name = "phenox_fig_zdb_id")
    private Figure figure;

    @Column(name = "phenox_created_date")
    private Date dateCreated;

    @Column(name = "phenox_last_modified")
    private Date dateLastModified;

    @OneToMany(mappedBy = "phenotypeExperiment")
    private Set<PhenotypeStatement> phenotypeStatements;

    @Override
    public String toString() {
        return "PhenotypeExperiment{" +
                "id=" + id  + '}';
    }

    public boolean hasObsoletePhenotype() {
        if(phenotypeStatements == null)
            return false;
        return phenotypeStatements.stream().anyMatch(PhenotypeStatement::hasObsoletePhenotype);
    }
}
