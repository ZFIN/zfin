package org.zfin.mutant;

import org.zfin.fish.repository.FishService;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;
import org.zfin.profile.FishSupplier;

import java.util.List;
import java.util.Set;

/**
 * Fish entity
 */
public class Fish implements EntityZdbID, Comparable<Fish> {

    public static final String WT = "WT";

    private String zdbID;
    private Genotype genotype;
    private String name;
    private String nameOrder;
    private String displayName;
    private String handle;
    private long order;
    private boolean wildtype;
    private Set<FishAlias> aliases;
    private Set<FishSupplier> suppliers;
    private Set<FishExperiment> fishExperiments;

    private List<SequenceTargetingReagent> strList;
    private long fishFunctionalAffectedGeneCount;

    public Set<FishAlias> getAliases() {
        if (aliases == null || aliases.size() == 0)
            return null;
        return aliases;
    }

    public void setAliases(Set<FishAlias> aliases) {
        this.aliases = aliases;
    }


    public long getFishFunctionalAffectedGeneCount() {
        return fishFunctionalAffectedGeneCount;
    }

    public void setFishFunctionalAffectedGeneCount(long fishFunctionalAffectedGeneCount) {
        this.fishFunctionalAffectedGeneCount = fishFunctionalAffectedGeneCount;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public boolean isWildtype() {
        return wildtype;
    }

    public void setWildtype(boolean wildtype) {
        this.wildtype = wildtype;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public List<SequenceTargetingReagent> getStrList() {
        return strList;
    }

    public void setStrList(List<SequenceTargetingReagent> strList) {
        this.strList = strList;
    }

    @Override
    public String getZdbID() {
        return zdbID;
    }

    @Override
    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

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

    public Set<FishExperiment> getFishExperiments() {
        return fishExperiments;
    }

    public void setFishExperiments(Set<FishExperiment> fishExperiments) {
        this.fishExperiments = fishExperiments;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
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

    public List<Marker> getAffectedGenes() {
        return FishService.getAffectedGenes(this);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isClean() {
        return fishFunctionalAffectedGeneCount == 0;
    }

    public Set<FishSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<FishSupplier> suppliers) {
        this.suppliers = suppliers;
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
