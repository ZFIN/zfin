package org.zfin.mutant;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.fish.repository.FishService;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.marker.Marker;

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
    private String handle;
    private long order;
    private boolean wildtype;

    private Set<FishExperiment> fishExperiments;

    private List<SequenceTargetingReagent> strList;
    private long fishFunctionalAffectedGeneCount;

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
        String newGenoName = genotype.getName();
        if (CollectionUtils.isNotEmpty(genotype.getAssociatedGenotypes())) {
            newGenoName += " ";
            newGenoName += genotype.getBackgroundDisplayName();
        }
        return name.replace(genotype.getName(), newGenoName);
    }

    public boolean isClean() {
        return fishFunctionalAffectedGeneCount == 0;
    }
}
