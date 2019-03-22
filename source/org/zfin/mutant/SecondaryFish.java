package org.zfin.mutant;
import org.zfin.marker.ReplacedData;
import javax.persistence.*;

@Entity
@DiscriminatorValue("Fish  ")
public class SecondaryFish extends ReplacedData {

    @ManyToOne
    @JoinColumn(name = "zrepld_new_zdb_id")
    private Fish fish;

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }
}
