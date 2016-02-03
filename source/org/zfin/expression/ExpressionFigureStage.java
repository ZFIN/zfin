package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.ComposedFxTerm;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
@Entity
@Table(name = "expression_figure_stage")
public class ExpressionFigureStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private DevelopmentStage startStage;
    @ManyToOne()
    @JoinColumn(name = "efs_end_stg_zdb_id")
    private DevelopmentStage endStage;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "expressionFigureStage", orphanRemoval = true)
    private Set<ExpressionResult2> expressionResultSet;

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage end) {
        this.endStage = end;
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

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage start) {
        this.startStage = start;
    }

    public Set<ExpressionResult2> getExpressionResultSet() {
        return expressionResultSet;
    }

    public void setExpressionResultSet(Set<ExpressionResult2> expressionResultSet) {
        this.expressionResultSet = expressionResultSet;
    }

    @Transient
    private List<ComposedFxTerm> terms;

    public List<ComposedFxTerm> getComposedTerms() {
        if (terms != null)
            return terms;
        terms = new ArrayList<>();
        for (ExpressionResult2 result : expressionResultSet) {
            ComposedFxTerm term = new ComposedFxTerm();
            term.setSuperTerm(result.getSuperTerm());
            term.setSubterm(result.getSubTerm());
            term.setExpressionFound(result.isExpressionFound());
            term.setID(result.getID());
            terms.add(term);
        }
        return terms;
    }


    public void addExpressionResult(ExpressionResult2 newExpressionResult) {
        if (expressionResultSet == null)
            expressionResultSet = new HashSet<>(1);
        expressionResultSet.add(newExpressionResult);
    }

    public boolean hasInvalidCombination() {
        if (expressionResultSet == null)
            return false;
        for (ExpressionResult2 result : expressionResultSet) {
            boolean absentPhenotypic = false;
            boolean eapNotAbsentPhenotypic = false;
            if (result.isEap()) {
                for (ExpressionPhenotypeTerm quality : result.getPhenotypeTermSet()) {
                    if (quality.isAbsentPhenotypic())
                        absentPhenotypic = true;
                    else
                        eapNotAbsentPhenotypic = true;
                }
            }
            return absentPhenotypic && eapNotAbsentPhenotypic;
        }
        return false;
    }
}

