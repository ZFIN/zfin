package org.zfin.mutant;

import org.zfin.infrastructure.EntityZdbID;

import java.util.List;
import java.util.Set;

/**
 * Fish entity
 */
public class Fish implements EntityZdbID {

    private String zdbID;
    private Genotype genotype;
    private String name;
    private String handle;
    private List<SequenceTargetingReagent> strList;

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
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
}
