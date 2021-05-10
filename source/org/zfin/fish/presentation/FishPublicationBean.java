package org.zfin.fish.presentation;

import org.zfin.fish.repository.FishService;
import org.zfin.mutant.Fish;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;

import java.util.Set;

public class FishPublicationBean extends PublicationListBean {

    protected Fish fish;

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    @Override
    public Set<Publication> getPublications() {
        return FishService.getFishPublications(fish);
    }
}
