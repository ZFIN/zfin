package org.zfin.mutant;

import org.zfin.fish.FishAnnotation;
import org.zfin.fish.MutationType;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.Marker;
import org.zfin.feature.Feature;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ConstructGeneFeature {

    private long ID;
    private ZfinEntity feature;
    private ZfinEntity gene;
    private String lab;

    public Feature getAllele() {
        return allele;
    }

    public void setAllele(Feature allele) {
        this.allele = allele;
    }

    public Marker getAffGene() {
        return affGene;
    }

    public void setAffGene(Marker affGene) {
        this.affGene = affGene;
    }

    private Feature allele;
    private Marker affGene;

    public String getLab() {
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

    public ZfinEntity getAlleleGene() {
        return alleleGene;
    }

    public void setAlleleGene(ZfinEntity alleleGene) {
        this.alleleGene = alleleGene;
    }

    public ConstructSearch getConstructSearch() {
        return constructSearch;
    }

    public void setConstructSearch(ConstructSearch constructSearch) {
        this.constructSearch = constructSearch;
    }

    private ZfinEntity alleleGene;

    private ConstructSearch constructSearch;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public ZfinEntity getFeature() {
        return feature;
    }

    public void setFeature(ZfinEntity feature) {
        this.feature = feature;
    }

    public ZfinEntity getGene() {
        return gene;
    }

    public void setGene(ZfinEntity gene) {
        this.gene = gene;
    }


}
