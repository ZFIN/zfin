package org.zfin.profile;

import org.zfin.mutant.Fish;

public class FishSupplier extends ObjectSupplier {

    private Fish fish;

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }
}
