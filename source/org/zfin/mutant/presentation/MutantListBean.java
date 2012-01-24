package org.zfin.mutant.presentation;


import org.zfin.mutant.Genotype;
import org.zfin.publication.Publication;
import org.zfin.marker.Marker;

import java.util.List;

public class MutantListBean {

    private Publication publication;
    private Marker gene;
    private List<Genotype> mutants;

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public List<Genotype> getMutants() {
        return mutants;
    }

    public void setMutants(List<Genotype> mutants) {
        this.mutants = mutants;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }
}