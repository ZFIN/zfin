package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
@Entity
@Table(name = "fish")
public class Fish implements EntityZdbID, Comparable<Fish> {

    public static final String WT = "WT";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Fish")
    @GenericGenerator(name = "Fish",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "FISH"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "fish_zdb_id")
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String zdbID;

    @ManyToOne
    @JoinColumn(name = "fish_genotype_zdb_id", nullable = false)
    private Genotype genotype;

    @Column(name = "fish_name")
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String name;

    @Column(name = "fish_name_order")
    private String nameOrder;

    @Column(name = "fish_full_name")
    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    private String displayName;

    @Column(name = "fish_handle")
    private String handle;

    @Column(name = "fish_order")
    private long order;

    @Column(name = "fish_is_wildtype")
    private boolean wildtype;

    @OneToMany(mappedBy = "fish")
    @OrderBy("aliasLowerCase")
    private Set<FishAlias> aliases;

    @OneToMany(mappedBy = "fish")
    private Set<SecondaryFish> secondaryFishSet;

    @OneToMany(mappedBy = "fish")
    private Set<FishSupplier> suppliers;

    @OneToMany(mappedBy = "fish")
    private Set<FishExperiment> fishExperiments;

    @Column(name = "fish_phenotypic_construct_count")
    private long fishPhenotypicConstructCount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "fish_str",
            joinColumns = @JoinColumn(name = "fishstr_fish_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "fishstr_str_zdb_id"))
    private List<SequenceTargetingReagent> strList;

    @Column(name = "fish_functional_affected_gene_count")
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
        if (!(o instanceof Fish fish)) return false;

        return zdbID != null ? zdbID.equals(fish.zdbID) : fish.zdbID == null;
    }

    @Override
    public int hashCode() {
        return zdbID != null ? zdbID.hashCode() : 0;
    }

}
