package org.zfin.profile;

import jakarta.persistence.*;
import org.zfin.mutant.Fish;

@Entity
@DiscriminatorValue("Fish  ")
public class FishSupplier extends ObjectSupplier {

    @ManyToOne
    @JoinColumn(name = "idsup_data_zdb_id", insertable = false, updatable = false)
    private Fish fish;

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }
}
