package org.zfin.marker.presentation;

import org.zfin.ExternalNote;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.marker.MarkerRelationship;
import org.zfin.profile.MarkerSupplier;

import java.util.List;
import java.util.Set;

/**
 */
public class AntibodyMarkerBean extends MarkerBean {


    //    private String antibodyNewPubZdbID;
    private List<ExternalNote> externalNotes ;
    private List<AnatomyLabel> antibodyDetailedLabelings;
    private List<MarkerSupplier> suppliers ;
    protected ExpressionSummaryCriteria expressionSummaryCriteria;
    private Set<MarkerRelationship> sortedAntigenRelationships;
    private Set<String> distinctAssayNames;
    private int numberOfPublications ;
    private int numberOfDistinctComposedTerms ;
    private List<MarkerRelationshipPresentation> antigenGenes;
    private String abRegistryID;
    private Antibody antibody;

    public String getAbRegistryID() {
        return abRegistryID;
    }

    public void setAbRegistryID(String abRegistryID) {
        this.abRegistryID = abRegistryID;
    }

    public ExpressionSummaryCriteria getExpressionSummaryCriteria() {
        return expressionSummaryCriteria;
    }

    public void setExpressionSummaryCriteria(ExpressionSummaryCriteria expressionSummaryCriteria) {
        this.expressionSummaryCriteria = expressionSummaryCriteria;
    }

    public void setAntibodyDetailedLabelings(List<AnatomyLabel> anatomyLabels){
        this.antibodyDetailedLabelings = anatomyLabels;
    }

    public List<AnatomyLabel> getAntibodyDetailedLabelings() {
        return antibodyDetailedLabelings;
    }

    public List<ExternalNote> getExternalNotes() {
        return externalNotes;
    }

    public void setExternalNotes(List<ExternalNote> externalNotes) {
        this.externalNotes = externalNotes;
    }

    public Set<MarkerRelationship> getSortedAntigenRelationships() {
        return sortedAntigenRelationships;
    }

    public void setSortedAntigenRelationships(Set<MarkerRelationship> sortedAntigenRelationships) {
        this.sortedAntigenRelationships = sortedAntigenRelationships;
    }

    public Set<String> getDistinctAssayNames() {
        return distinctAssayNames;
    }

    public void setDistinctAssayNames(Set<String> distinctAssayNames) {
        this.distinctAssayNames = distinctAssayNames;
    }


    public String getEditURL() {
        String zdbID = marker.getZdbID();
        return "/action/marker/marker-edit?zdbID=" + zdbID;
    }

    public String getDeleteURL() {
        String zdbID = marker.getZdbID();
        return "/action/infrastructure/deleteRecord/" + zdbID;
    }

    public String getMergeURL() {
        String zdbID = marker.getZdbID();
        return "/action/marker/merge?zdbIDToDelete=" + zdbID;
    }

    public List<MarkerSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<MarkerSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public int getNumberOfDistinctComposedTerms() {
        return numberOfDistinctComposedTerms;
    }

    public void setNumberOfDistinctComposedTerms(int numberOfDistinctComposedTerms) {
        this.numberOfDistinctComposedTerms = numberOfDistinctComposedTerms;
    }

    public List<MarkerRelationshipPresentation> getAntigenGenes() {
        return antigenGenes;
    }

    public void setAntigenGenes(List<MarkerRelationshipPresentation> antigenGenes) {
        this.antigenGenes = antigenGenes;
    }

    public int getNumberOfPublications() {
        return numberOfPublications;
    }

    public void setNumberOfPublications(int numberOfPublications) {
        this.numberOfPublications = numberOfPublications;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }
}