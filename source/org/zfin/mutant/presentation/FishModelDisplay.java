package org.zfin.mutant.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.expression.Experiment;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.mutant.DiseaseModel;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishModel;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * Disease model which groups by Publications
 */
public class FishModelDisplay implements Comparable<FishModelDisplay> {

    private Set<Publication> publications;
    private FishModel fishModel;


    public FishModelDisplay(FishModel fishModel) {
        this.fishModel = fishModel;
    }

    public FishModel getFishModel() {
        return fishModel;
    }

    public void setFishModel(FishModel fishModel) {
        this.fishModel = fishModel;
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public Publication getPublication() {
        if (CollectionUtils.isEmpty(publications))
            return null;
        return publications.iterator().next();
    }

    public void addPublication(Publication publication) {
        if (publications == null)
            publications = new HashSet<>();
        publications.add(publication);
    }

    @Override
    public int compareTo(FishModelDisplay o) {
        if (o == null)
            return 1;
        if (fishModel.getFish().getName().equals(o.getFishModel().getFish().getName()))
            return fishModel.getExperiment().getName().compareTo(o.getFishModel().getExperiment().getName());
        return fishModel.getFish().getName().compareTo(o.getFishModel().getFish().getName());
    }
}
