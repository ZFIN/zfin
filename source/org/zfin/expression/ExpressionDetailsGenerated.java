package org.zfin.expression;

import org.zfin.antibody.Antibody;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.FishExperiment;
import org.zfin.sequence.MarkerDBLink;

import java.util.Set;

/**
 * Mapping of expression_experiment2 table which
 * moves figure into expression_experiment, and
 * uses a pk id rather than a zdb_id
 */
public class ExpressionDetailsGenerated {
    private long id;
    private Figure figure;
    private Set<ExpressionResultGenerated> expressionResults;
    private ExpressionExperiment expressionExperiment;
    private FishExperiment fishExperiment;
    private Marker gene;
    private Clone probe;
    private ExpressionAssay assay;
    private Antibody antibody;
    // this markerdblink refers to either the probe or the gene as far as I can tell.  Mostly the gene, though.
    private MarkerDBLink markerDBLink;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Set<ExpressionResultGenerated> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(Set<ExpressionResultGenerated> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public ExpressionExperiment getExpressionExperiment() { return expressionExperiment; }

    public void setExpressionExperiment(ExpressionExperiment expressionExperiment) { this.expressionExperiment = expressionExperiment; }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Clone getProbe() {
        return probe;
    }

    public void setProbe(Clone probe) {
        this.probe = probe;
    }

    public ExpressionAssay getAssay() {
        return assay;
    }

    public void setAssay(ExpressionAssay assay) {
        this.assay = assay;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public MarkerDBLink getMarkerDBLink() {
        return markerDBLink;
    }

    public void setMarkerDBLink(MarkerDBLink markerDBLink) {
        this.markerDBLink = markerDBLink;
    }
}
