package org.zfin.mutant;

import org.zfin.infrastructure.DataAlias;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("Fish  ")
public class FishAlias extends DataAlias {

    @ManyToOne
    @JoinColumn(name = "dalias_data_zdb_id")
    private Fish fish;

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
        dataZdbID = fish.getZdbID();
    }

}