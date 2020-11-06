package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;

import java.util.SortedSet;

/**
 * This class is a statistics class about Fish for given genotype
 */
@Setter
@Getter
public class GenotypeFishResult implements Comparable<GenotypeFishResult> {

    @JsonView(View.API.class)
    private Fish fish;
    @JsonView(View.API.class)
    private SortedSet<Marker> affectedMarkers;
    @JsonView(View.API.class)
    private FishGenotypePhenotypeStatistics fishGenotypePhenotypeStatistics;
    @JsonView(View.API.class)
    private FishGenotypeExpressionStatistics fishGenotypeExpressionStatistics;
    @JsonView(View.API.class)
    private String zygosity;
    @JsonView(View.API.class)
    private String parentalZygosity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenotypeFishResult that = (GenotypeFishResult) o;

        return fish.getAbbreviation().equals(that.fish.getAbbreviation());

    }

    @Override
    public int hashCode() {
        return fish.hashCode();
    }

    @Override
    public int compareTo(GenotypeFishResult o) {
        return fish.getAbbreviation().compareTo(o.getFish().getAbbreviation());
    }
}
