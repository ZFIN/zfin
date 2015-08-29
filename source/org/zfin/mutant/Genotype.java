package org.zfin.mutant;

import org.springframework.util.CollectionUtils;
import org.zfin.fish.FishAnnotation;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.GenotypeSupplier;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * This class defines a genotype, typically provided if at least one allele
 * is different from the wild type, i.e. at least a single site mutant.
 * A single mutant belonging to one genotype can be part of another genotype
 * as double mutant or in a genotype in which there are more than two alleles
 * are affected. However, each genotype is its own object.
 * The name of the genotype is a semicolon-delimited list of allele names.
 * Each allele is called a Feature
 */
public class Genotype implements Comparable, EntityZdbID {

    public static final String WT = "WT";

    private String zdbID;
    private String name;
    private String nameOrder;
    private String handle;
    private String nickname;
    private boolean wildtype;
    private boolean extinct;
    // This attribute is used only for storage purposes.
    // as the background is stored as a many-to-many relationship.
    private Set<Genotype> associatedGenotypes;
    private Set<GenotypeFeature> genotypeFeatures;
    private Set<GenotypeExternalNote> externalNotes;
    private Set<DataNote> dataNotes;
    private Set<GenotypeSupplier> suppliers;
    private Set<GenotypeAlias> aliases;

    private String complexity;
    private List<Publication> associatedPulications;

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

    public void setBackground(Genotype background) {
        if (CollectionUtils.isEmpty(associatedGenotypes))
            associatedGenotypes = new HashSet<>();
        associatedGenotypes.add(background);
    }

    public Set<GenotypeFeature> getGenotypeFeatures() {
        return genotypeFeatures;
    }

    public void setGenotypeFeatures(Set<GenotypeFeature> genotypeFeatures) {
        this.genotypeFeatures = genotypeFeatures;
    }

    /**
     * Only checking against the zdb id for now
     *
     * @param otherGenotype to compare for equality
     * @return boolean for equality
     */
    public boolean equals(Object otherGenotype) {
        if (!(otherGenotype instanceof Genotype)) {
            if (otherGenotype instanceof FishAnnotation) {
                return ((FishAnnotation) otherGenotype).getGenotypeID().equals(getZdbID());
            } else {
                return false;
            }
        }
        Genotype og = (Genotype) otherGenotype;
        return getZdbID().equals(og.getZdbID());
    }

    public int compareTo(Object o) {
        Genotype otherGenotype = (Genotype) o;

        if (getComplexity().compareTo(otherGenotype.getComplexity()) != 0) {
            return getComplexity().compareTo(otherGenotype.getComplexity());
        }

        return getNameOrder().compareTo(otherGenotype.getNameOrder());
    }

    public Set<GenotypeExternalNote> getExternalNotes() {
        return externalNotes;
    }

    public void setExternalNotes(Set<GenotypeExternalNote> externalNotes) {
        this.externalNotes = externalNotes;
    }

    public Set<DataNote> getDataNotes() {
        if (dataNotes == null) {
            return new HashSet<>();
        }
        return dataNotes;
    }

    public void setDataNotes(Set<DataNote> dataNotes) {
        this.dataNotes = dataNotes;
    }

    public SortedSet<DataNote> getSortedDataNotes() {
        return new TreeSet(this.getDataNotes());
    }

    public Set<GenotypeSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<GenotypeSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public Set<GenotypeAlias> getAliases() {
        return aliases;
    }

    public void setAliases(Set<GenotypeAlias> aliases) {
        this.aliases = aliases;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    @Override
    public String getAbbreviation() {
        return name;
    }

    @Override
    public String getAbbreviationOrder() {
        return nameOrder;
    }

    @Override
    public String getEntityType() {
        return "Genotype";
    }

    @Override
    public String getEntityName() {
        return name;
    }

    public String getBackgroundDisplayName() {
        String backgroundDisplay = " ";
        if (!CollectionUtils.isEmpty(associatedGenotypes)) {
            for (Genotype background : associatedGenotypes) {
                backgroundDisplay += "(";
                backgroundDisplay += background.getHandle();
                backgroundDisplay += "), ";
            }
            backgroundDisplay = backgroundDisplay.substring(0, backgroundDisplay.length() - 2);
        }
        return backgroundDisplay;
    }


    /* Only putting TU in for now, since it's the only wildtype that's specified by name
     * rather than generically looking at the isWildtype boolean */
    public enum Wildtype {
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

    public boolean isExtinct() {
        return extinct;
    }

    public void setExtinct(boolean extinct) {
        this.extinct = extinct;
    }

    public List<Publication> getAssociatedPublications() {
        if (associatedPulications == null) {
            associatedPulications = RepositoryFactory.getPublicationRepository().getAllAssociatedPublicationsForGenotype(this, -1).getPopulatedResults();
        }
        return associatedPulications;
    }

}
