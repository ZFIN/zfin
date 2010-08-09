package org.zfin.mutant;

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
public class Genotype implements Comparable {

    public static final String WT = "WT";

    private String zdbID;
    private String name;
    private String nameOrder;
    private String handle;
    private String nickname;
    private boolean wildtype;
    private Set<GenotypeExperiment> genotypeExperiments;
    // This attribute is used only for storage purposes.
    // as the background is stored as a many-to-many relationship.
    private Set<Genotype> associatedGenotypes;
    private Set<GenotypeFeature> genotypeFeatures;

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

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * There is only one background per genotype, so ensure unquiness
     *
     * @return genotype
     */
    public Set<Genotype> getAssociatedGenotypes() {
        return associatedGenotypes;
    }

    public void setAssociatedGenotypes(Set<Genotype> associatedGenotypes) {
        this.associatedGenotypes = associatedGenotypes;
    }
    /*public Genotype getBackground() {
      if (CollectionUtils.isEmpty(associatedGenotypes))
          return null;

    *//*  if (associatedGenotypes.size() > 1)
            throw new RuntimeException("Found more than one associated genotype (Background)! " + associatedGenotypes);*//*
        Iterator<Genotype> iterator = associatedGenotypes.iterator();
        return iterator.next();
    }

    *
    public void setBackground(Genotype background) {
       *//*if (!CollectionUtils.isEmpty(associatedGenotypes))
            throw new RuntimeException("Found already one associated genotype (Background)! " + associatedGenotypes);*//*
        associatedGenotypes = new HashSet<Genotype>();
        associatedGenotypes.add(background);
    }*/

    public Set<GenotypeFeature> getGenotypeFeatures() {
        return genotypeFeatures;
    }

    public void setGenotypeFeatures(Set<GenotypeFeature> genotypeFeatures) {
        this.genotypeFeatures = genotypeFeatures;
    }

    /**
     * Only checking against the zdb id for now
     * @param otherGenotype to compare for equality
     * @return boolean for equality
     */
    public boolean equals(Object otherGenotype) {
        if (!(otherGenotype instanceof Genotype))
            return false;
        Genotype og = (Genotype)otherGenotype;
        return getZdbID().equals(og.getZdbID());
    }

    public int compareTo(Object o) {
        Genotype otherGenotype = (Genotype)o;
        return getNameOrder().compareTo(otherGenotype.getNameOrder());
    }


    /* Only putting TU in for now, since it's the only wildtype that's specified by name
     * rather than generically looking at the isWildtype boolean */
    public static enum Wildtype {
        TU("TU");

        private final String value;

        private Wildtype(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static Wildtype getType(String type) {
            for (Wildtype t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }

    }


}
