package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.FishExperiment;
import org.zfin.sequence.MarkerDBLink;

import java.util.Set;

/**
 * Mapping of expression_experiment2 table which
 * moves figure into expression_experiment2, and
 * uses a pk id rather than a zdb_id
 */
@Entity
@Table(name = "xpat_exp_details_generated")
@Getter
@Setter
public class ExpressionDetailsGenerated {

    @Id
    @Column(name = "xedg_pk_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_fig_zdb_id")
    private Figure figure;

    @OneToMany(mappedBy = "expressionExperiment", fetch = FetchType.LAZY)
    private Set<ExpressionResultGenerated> expressionResults;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_zdb_id")
    private ExpressionExperiment2 expressionExperiment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_genox_zdb_id")
    private FishExperiment fishExperiment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_gene_zdb_id")
    private Marker gene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_probe_feature_zdb_id")
    private Clone probe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_assay_name")
    private ExpressionAssay assay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_atb_zdb_id")
    private Antibody antibody;

    // this markerdblink refers to either the probe or the gene as far as I can tell.  Mostly the gene, though.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xedg_dblink_zdb_id")
    private MarkerDBLink markerDBLink;
}
