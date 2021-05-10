package org.zfin.mutant;

import org.zfin.infrastructure.DataAlias;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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