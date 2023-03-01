package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;
import org.zfin.profile.FishSupplier;

import java.util.List;
import java.util.Set;

/**
 * Fish entity
 */
@Setter
@Getter
public class Fish implements EntityZdbID, Comparable<Fish> {

    public static final String WT = "WT";

    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String zdbID;
    private Genotype genotype;
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String name;
    private String nameOrder;
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String displayName;
    private String handle;
    private long order;
    private boolean wildtype;
    private Set<FishAlias> aliases;
    private Set<SecondaryFish> secondaryFishSet;
    private Set<FishSupplier> suppliers;
    private Set<FishExperiment> fishExperiments;
    private long fishPhenotypicConstructCount;

    private List<SequenceTargetingReagent> strList;
    private long fishFunctionalAffectedGeneCount;

    public Set<FishAlias> getAliases() {
        if (aliases == null || aliases.size() == 0)
            return null;
        return aliases;
    }

    @JsonView({View.FishAPI.class, View.ExpressedGeneAPI.class})
    @Override
    public String getAbbreviation() {
        return name;
    }

    @Override
    public String getAbbreviationOrder() {
        return name;
    }

    @Override
    public String getEntityType() {
        return "Fish";
    }

    @Override
    public String getEntityName() {
        return "Fish";
    }

    public String getFishID() {
        return zdbID;
    }

    public int compareTo(Fish otherFish) {
        if (order != otherFish.getOrder())
            return order < otherFish.getOrder() ? -1 : 1;
        return getNameOrder().compareTo(otherFish.getNameOrder());
    }

    public boolean isWildtypeWithoutReagents() {
        return genotype.isWildtype() && strList.size() == 0;
    }

    public String toString() {
        return name;
    }

    @JsonView(View.ExpressedGeneAPI.class)
    public List<Marker> getAffectedGenes() {
        return FishService.getAffectedGenes(this);
    }

    public boolean isClean() {
        return fishFunctionalAffectedGeneCount == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fish)) return false;

        Fish fish = (Fish) o;

        return zdbID != null ? zdbID.equals(fish.zdbID) : fish.zdbID == null;
    }

    @Override
    public int hashCode() {
        return zdbID != null ? zdbID.hashCode() : 0;
    }

}
