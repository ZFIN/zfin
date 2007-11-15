package org.zfin.expression;

import org.zfin.publication.Publication;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.Gene;
import org.zfin.mutant.GenotypeExperiment;

import java.util.Set;

/**
 * Expression ExpressionExperiment.
 */
public class ExpressionExperiment {

    private String zdbID;
    private String publicationID;
    private String cloneID;
    private String geneID;
    private Publication publication;
    private Clone clone;
    private Set<ExpressionResult> expressionResults;
    private GenotypeExperiment genotypeExperiment;
    private Marker marker;
    private Marker probe;
    private Gene gene;

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

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
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

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }
}
