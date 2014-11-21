package org.zfin.marker.presentation;

import org.zfin.mutant.Genotype;
import org.zfin.mutant.repository.FeaturePresentationBean;

import java.util.List;

/**
 */
public class MutantOnMarkerBean {

    private List<FeaturePresentationBean> alleles;
    private List<MarkerRelationshipPresentation> knockdownReagents;
    private List<Genotype> genotypeList;

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

    public List<Genotype> getGenotypeList() {
        return genotypeList;
    }

    public void setGenotypeList(List<Genotype> genotypeList) {
        this.genotypeList = genotypeList;
    }
}
