package org.zfin.marker.presentation;

import org.zfin.mutant.Genotype;
import org.zfin.mutant.repository.FeaturePresentationBean;
import org.zfin.feature.Feature;

import java.util.List;

/**
 *
 */
public class MutantOnMarkerBean {

    private List<Feature> features;
    private List<FeaturePresentationBean> alleles;
    private List<SequenceTargetingReagentBean> knockdownReagents;
    private List<Genotype> genotypeList;

    public List<FeaturePresentationBean> getAlleles() {
        return alleles;
    }

    public void setAlleles(List<FeaturePresentationBean> alleles) {
        this.alleles = alleles;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public List<SequenceTargetingReagentBean> getKnockdownReagents() {
        return knockdownReagents;
    }

    public void setKnockdownReagents(List<SequenceTargetingReagentBean> knockdownReagents) {
        this.knockdownReagents = knockdownReagents;
    }

    public List<Genotype> getGenotypeList() {
        return genotypeList;
    }

    public void setGenotypeList(List<Genotype> genotypeList) {
        this.genotypeList = genotypeList;
    }
}
