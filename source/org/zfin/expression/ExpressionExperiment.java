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
    private Publication publication;
    private Set<ExpressionResult> expressionResults;
    private GenotypeExperiment genotypeExperiment;
    private Marker gene;
    private Clone probe;
    private ExpressionAssay assay;
    private Antibody antibody;
    // this markerdblink refers to either the probe or the gene as far as I can tell.  Mostly the gene, though.
    private MarkerDBLink markerDBLink;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public int getAlternateKey(){
        int hash = 1;
        hash = hash * 31 + publication.hashCode();  // uses zdbID
        hash = hash * 31 + genotypeExperiment.getZdbID().hashCode();
        hash = hash * 31 + assay.getName().hashCode();
        hash = hash * 31 + (probe== null ? 0 : probe.hashCode()); // uses zdbID
        hash = hash * 31 + (gene== null ? 0 : gene.hashCode());// uses zdbID
        // dblink
        hash = hash * 31 + (markerDBLink== null ? 0 : markerDBLink.hashCode());// uses zdbID
        // atb
        hash = hash * 31 + (antibody== null ? 0 : antibody.hashCode());// uses zdbID

        return hash;
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

    public ExpressionResult getMatchingExpressionResult(ExpressionResult expressionResult) {
        for(ExpressionResult aExpressionResult:getExpressionResults()){
            if(false==canMergeExpressionResult(aExpressionResult,expressionResult)){
                return aExpressionResult ;
            }
        }
        return null ; 
    }

    /**
     * Uses alternate key:
     * experiment, anatomy item, start stage, end stage, expression found, and term
     * Only term can be null.  Expression found is a boolean.
     * Since experiment is going ot be moved, we don't really care about that.
     * @param era First expresion result.
     * @param erb Second expression result.
     * @return Indicates if these records are too similar (false) or not (true).
     */
    private boolean canMergeExpressionResult(ExpressionResult era,ExpressionResult erb){
        if(!era.getSuperterm().equals(erb.getSuperterm())) return true ;
        if(!era.getStartStage().equals(erb.getStartStage())) return true ;
        if(!era.getEndStage().equals(erb.getEndStage())) return true ;
        if(!era.isExpressionFound()==erb.isExpressionFound()) return true ;
        if(era.getSubterm()==null && erb.getSubterm()!=null) return true ;
        if(era.getSubterm()!=null && erb.getSubterm()==null) return true ;
        if(era.getSubterm()!=null && erb.getSubterm()!=null &&
                false==era.getSubterm().equals(erb.getSubterm())) return true ;

        return false ;
    }
}
