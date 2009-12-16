package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.publication.Publication;
import org.zfin.sequence.MarkerDBLink;

import java.util.HashSet;
import java.util.Set;

/**
 * Main Experiment object that contains expression annotations.
 */
public class ExpressionExperiment {

    private String zdbID;
    private String cloneID;
    private String geneID;
    private Publication publication;
    private Clone clone;
    private Set<ExpressionResult> expressionResults;
    private GenotypeExperiment genotypeExperiment;
    private Marker marker;
    private Marker probe;
    private ExpressionAssay assay;
    private Antibody antibody;
    private MarkerDBLink markerDBLink;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getGeneID() {
        return geneID;
    }

    public void setGeneID(String geneID) {
        this.geneID = geneID;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Set<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(Set<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public Clone getClone() {
        return clone;
    }

    public void setClone(Clone clone) {
        this.clone = clone;
    }

    public String getCloneID() {
        return cloneID;
    }

    public void setCloneID(String cloneID) {
        this.cloneID = cloneID;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getProbe() {
        return probe;
    }

    public void setProbe(Marker probe) {
        this.probe = probe;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public ExpressionAssay getAssay() {
        return assay;
    }

    public void setAssay(ExpressionAssay assay) {
        this.assay = assay;
    }

    public MarkerDBLink getMarkerDBLink() {
        return markerDBLink;
    }

    public void setMarkerDBLink(MarkerDBLink markerDBLink) {
        this.markerDBLink = markerDBLink;
    }

    /**
     * Distinct expressions are combinations of
     * 1) Figure
     * 2) Stage Range
     * You can add multiple structures to such a combination.
     *
     * @return number of distinct expressions
     */
    public int getDistinctExpressions() {
        HashSet<String> distinctSet = new HashSet<String>();
        if (expressionResults != null) {
            for (ExpressionResult expression : expressionResults) {
                DevelopmentStage startStage = expression.getStartStage();
                DevelopmentStage endStage = expression.getEndStage();
                Set<Figure> figures = expression.getFigures();
                for (Figure figure : figures) {
                    StringBuilder sb = new StringBuilder(figure.getZdbID());
                    sb.append(startStage.getZdbID());
                    sb.append(endStage.getZdbID());
                    distinctSet.add(sb.toString());
                }
            }
        }
        return distinctSet.size();
    }

    public void addExpressionResult(ExpressionResult newResult) {
        if (expressionResults == null)
            expressionResults = new HashSet<ExpressionResult>();
        expressionResults.add(newResult);
    }
}
