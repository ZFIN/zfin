package org.zfin.mutant.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.mutant.FishExperiment;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * Disease model which groups by Publications
 */
public class FishModelDisplay implements Comparable<FishModelDisplay> {

    private Set<Publication> publications;
    private FishExperiment fishModel;


    public FishModelDisplay(FishExperiment fishModel) {
        this.fishModel = fishModel;
    }

    public FishExperiment getFishModel() {
        return fishModel;
    }

    public void setFishModel(FishExperiment fishModel) {
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
