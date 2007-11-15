package org.zfin.mutant;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class defines a genotype, typically provided if at least one allele
 * is different from the wild type, i.e. at least a single site mutant.
 * A single mutant belonging to one genotype can be part of another genotype
 * as double mutant or in a genotype in which there are more than two alleles
 * are affected. However, each genotype is its own object.
 * The name of the genotype is a semicolon-delimited list of allele names.
 * Each allele is called a Feature
 */
public class Genotype {

    private String zdbID;
    private String name;
    private String nameOrder;
    private boolean wildtype;
    private Set<GenotypeExperiment> genotypeExperiments;
    // This attribute is used only for storage purposes.
    // as the background is stored as a many-to-many relationship.
    private Set<Genotype> associatedGenotypes;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
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


    public boolean isWildtype() {
        return wildtype;
    }

    public void setWildtype(boolean wildtype) {
        this.wildtype = wildtype;
    }

    public Set<GenotypeExperiment> getGenotypeExperiments() {
        return genotypeExperiments;
    }

    public void setGenotypeExperiments(Set<GenotypeExperiment> genotypeExperiments) {
        this.genotypeExperiments = genotypeExperiments;
    }

    /**
     * There is only one background per genotype, so ensure unquiness
     *
     * @return genotype
     */
    public Genotype getBackground() {
        if (CollectionUtils.isEmpty(associatedGenotypes))
            return null;

        if (associatedGenotypes.size() > 1)
            throw new RuntimeException("Found more than one associated genotype (Background)! " + associatedGenotypes);
        Iterator<Genotype> iterator = associatedGenotypes.iterator();
        return iterator.next();
    }

    /**
     * Make sure we have not already added a background. Only one allowed.
     * @param background Genotype
     */
    public void setBackground(Genotype background) {
       if (!CollectionUtils.isEmpty(associatedGenotypes))
            throw new RuntimeException("Found already one associated genotype (Background)! " + associatedGenotypes);
        associatedGenotypes = new HashSet<Genotype>();
        associatedGenotypes.add(background);
    }

}
