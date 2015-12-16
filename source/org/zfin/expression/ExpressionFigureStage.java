package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.FishExperiment;
import org.zfin.publication.Publication;
import org.zfin.sequence.MarkerDBLink;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
@Entity
@Table(name = "expression_figure_stage")
public class ExpressionFigureStage {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "efs_pk_id")
    private long id;
    @ManyToOne()
    @JoinColumn(name = "efs_xpatex_zdb_id")
    private ExpressionExperiment2 expressionExperiment;
    @ManyToOne()
    @JoinColumn(name = "efs_fig_zdb_id")
    private Figure figure;
    @ManyToOne()
    @JoinColumn(name = "efs_start_stg_zdb_id")
    private DevelopmentStage start;
    @ManyToOne()
    @JoinColumn(name = "efs_end_stg_zdb_id")
    private DevelopmentStage end;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "expressionFigureStage")
    private Set<ExpressionResult2> expressionResultSet;

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }

    public ExpressionExperiment2 getExpressionExperiment() {
        return expressionExperiment;
    }

    public void setExpressionExperiment(ExpressionExperiment2 expressionExperiment) {
        this.expressionExperiment = expressionExperiment;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
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

    public Set<ExpressionResult2> getExpressionResultSet() {
        return expressionResultSet;
    }

    public void setExpressionResultSet(Set<ExpressionResult2> expressionResultSet) {
        this.expressionResultSet = expressionResultSet;
    }
}

