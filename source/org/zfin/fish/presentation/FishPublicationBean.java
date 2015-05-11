package org.zfin.fish.presentation;

import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FishPublicationBean extends PublicationListBean {

    protected MartFish fish;

    public MartFish getFish() {
        return fish;
    }

    public void setFish(MartFish fish) {
        this.fish = fish;
    }

    @Override
    public Set<Publication> getPublications() {
        List<Publication> pubs =RepositoryFactory.getMutantRepository().getFishAttributionList(fish.getGenotypeExperimentIDs());
        Set<Publication> publicationSet = new HashSet<Publication>(pubs.size());
        publicationSet.addAll(pubs);
        return publicationSet;
    }
}
