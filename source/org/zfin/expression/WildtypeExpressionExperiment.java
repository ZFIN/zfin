package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;

/**
 * TODO: remove this if we no longer need it
 */
public class WildtypeExpressionExperiment {
    private Long id;
    private ExpressionExperiment expressionExperiment;
    private DevelopmentStage startStage ;
    private DevelopmentStage endStage ;
    private Marker gene ;
    private GenericTerm superTerm;
    private GenericTerm subTerm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpressionExperiment getExpressionExperiment() {
        return expressionExperiment;
    }

    public void setExpressionExperiment(ExpressionExperiment expressionExperiment) {
        this.expressionExperiment = expressionExperiment;
    }

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public GenericTerm getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(GenericTerm superTerm) {
        this.superTerm = superTerm;
    }

    public GenericTerm getSubTerm() {
        return subTerm;
    }

    public void setSubTerm(GenericTerm subTerm) {
        this.subTerm = subTerm;
    }
}
