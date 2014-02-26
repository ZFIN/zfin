package org.zfin.marker.presentation;

import org.zfin.mutant.repository.FeaturePresentationBean;

import java.util.List;

/**
 */
public class MutantOnMarkerBean {

    private List<FeaturePresentationBean> alleles;
    private List<MarkerRelationshipPresentation> knockdownReagents ;
    private String mutantLineDisplay;
    private Boolean hasOrderThisLinks;

    public Boolean getHasOrderThisLinks() {
        return hasOrderThisLinks;
    }

    public void setHasOrderThisLinks(Boolean hasOrderThisLinks) {
        this.hasOrderThisLinks = hasOrderThisLinks;
    }

    public List<FeaturePresentationBean> getAlleles() {
        return alleles;
    }

    public void setAlleles(List<FeaturePresentationBean> alleles) {
        this.alleles = alleles;
    }

    public List<MarkerRelationshipPresentation> getKnockdownReagents() {
        return knockdownReagents;
    }

    public void setKnockdownReagents(List<MarkerRelationshipPresentation> knockdownReagents) {
        this.knockdownReagents = knockdownReagents;
    }

    public String getMutantLineDisplay() {
        return mutantLineDisplay;
    }

    public void setMutantLineDisplay(String mutantLineDisplay) {
        this.mutantLineDisplay = mutantLineDisplay;
    }
}
