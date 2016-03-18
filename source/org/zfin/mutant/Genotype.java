package org.zfin.mutant;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.CollectionUtils;
import org.zfin.fish.FishAnnotation;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.profile.GenotypeSupplier;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import javax.persistence.*;
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
@Entity
@Table(name = "genotype")
public class Genotype implements Comparable, EntityZdbID {

    public static final String WT = "WT";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "GENO"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "geno_zdb_id")
    private String zdbID;
    @Column(name = "geno_display_name")
    private String name;
    @Column(name = "geno_name_order")
    private String nameOrder;
    @Column(name = "geno_handle")
    private String handle;
    @Column(name = "geno_nickname")
    private String nickname;
    @Column(name = "geno_is_wildtype")
    private boolean wildtype;
    @Column(name = "geno_is_extinct")
    private boolean extinct;
    // This attribute is used only for storage purposes.
    // as the background is stored as a many-to-many relationship.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "genotype_background", joinColumns = {
            @JoinColumn(name = "genoback_geno_zdb_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "genoback_background_zdb_id",
                    nullable = false, updatable = false)})
    private Set<Genotype> associatedGenotypes;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, mappedBy = "genotype")
    private Set<GenotypeFeature> genotypeFeatures;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "genotype")
    private Set<GenotypeExternalNote> externalNotes;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dnote_data_zdb_id")
    private Set<DataNote> dataNotes;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "genotype")
    private Set<GenotypeSupplier> suppliers;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "genotype")
    private Set<GenotypeAlias> aliases;

    @Column(name = "geno_complexity_order")
    private String complexity;
    @Transient
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
