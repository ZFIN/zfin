package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;

import javax.persistence.*;

/**
 * An individual observation of phenotype
 */
@Entity
@Table(name = "phenotype_source_generated")
@Setter
@Getter
public class PhenotypeSourceGenerated {


    @Id
    @Column(name = "pg_id")
    private long id;

    @JsonView(View.API.class)
    @ManyToOne
    @JoinColumn(name = "pg_genox_zdb_id")
    private FishExperiment fishExperiment;

    @JsonView(View.API.class)
    @ManyToOne
    @JoinColumn(name = "pg_fig_zdb_id")
    private Figure figure;

    @JsonView(View.API.class)
    @ManyToOne
    @JoinColumn(name = "pg_start_stg_zdb_id")
    private DevelopmentStage start;

    @JsonView(View.API.class)
    @ManyToOne
    @JoinColumn(name = "pg_end_stg_zdb_id")
    private DevelopmentStage end;

}
