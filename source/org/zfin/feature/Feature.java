package org.zfin.feature;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortNatural;
import org.zfin.feature.service.MutationDetailsConversionService;
import org.zfin.framework.api.View;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityNotes;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.profile.FeatureSource;
import org.zfin.profile.FeatureSupplier;
import org.zfin.sequence.FeatureDBLink;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feature business entity.
 */
@Entity
@Setter
@Getter
@Table(name = "feature")
//@Audited
// Only update attributes that have changed.
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Feature implements EntityNotes, EntityZdbID {

    // TODO: can this be managed by spring?
    @Transient
    MutationDetailsConversionService mutationDetailsConversionService = new MutationDetailsConversionService();

    public static final String MUTANT = "Feature";
    public static final String UNRECOGNIZED = "unrecognized";
    public static final String UNSPECIFIED = "unspecified";
    public static final String UNKNOWN = "Unknown";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Feature")
    @GenericGenerator(name = "Feature",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "ALT"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "feature_zdb_id")
    @JsonView(View.API.class)
    private String zdbID;
    @Column(name = "feature_name", nullable = false)
    //@Audited
    @JsonView(View.FeatureAPI.class)
    private String name;
    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<FeatureNote> externalNotes;
    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)

    @Transient
    private String publicComments;
    @Column(name = "feature_line_number")
    private String lineNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_lab_prefix_id")
    private FeaturePrefix featurePrefix;
    @Column(name = "feature_abbrev", nullable = false)
    @JsonView(View.API.class)
    private String abbreviation;
    @Column(name = "feature_tg_suffix")
    private String transgenicSuffix;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dnote_data_zdb_id")
    private Set<DataNote> dataNotes;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "recattrib_data_zdb_id")
    private Set<PublicationAttribution> publications;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "feature", fetch = FetchType.LAZY)
    private Set<GenotypeFeature> genotypeFeatures;
    @Column(name = "feature_abbrev_order", nullable = false)
    private String abbreviationOrder;
    @Column(name = "feature_name_order", nullable = false)
    private String nameOrder;
    @Column(name = "feature_known_insertion_site")
    private Boolean isKnownInsertionSite;
    @Column(name = "feature_dominant")
    private Boolean isDominantFeature;
    @Column(name = "feature_unspecified")
    private Boolean isUnspecifiedFeature;
    @Column(name = "ftr_chr_info_date")
    private Date ftrAssemblyInfoDate;
    @Column(name="feature_date_entered")
    private Date ftrEntryDate;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "feature", fetch = FetchType.LAZY)
    @SortNatural
    private Set<FeatureMarkerRelationship> featureMarkerRelations;
    @Column(name = "feature_type")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.gwt.root.dto.FeatureTypeEnum")})
    @JsonView(View.FeatureAPI.class)
    private FeatureTypeEnum type;
    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY)
    private Set<FeatureSupplier> suppliers;
    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY)
    private Set<FeatureSource> sources;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "feature", fetch = FetchType.LAZY)
    @org.hibernate.annotations.OrderBy(clause = "dalias_alias_lower")
    private Set<FeatureAlias> aliases;
    @OneToOne(mappedBy = "feature", fetch = FetchType.EAGER)
    @JsonView(View.FeatureAPI.class)
    private FeatureAssay featureAssay;

    @OneToMany(mappedBy = "feature", fetch = FetchType.EAGER)
    private Set<FeatureDBLink> dbLinks;
    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @SortNatural
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private SortedSet<FeatureTranscriptMutationDetail> featureTranscriptMutationDetailSet;

    //I followed the same mapping for feature protein mutation details and dna mutation details(Prita)
    //I added the SAVE_UPDATE cascade type to ensure updates, inserts and deletes. (case 14286)

    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Set<FeatureProteinMutationDetail> featureProteinMutationDetailSet;

    @Transient
    private FeatureProteinMutationDetail featureProteinMutationDetail;

    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Set<FeatureDnaMutationDetail> featureDnaMutationDetailSet;


    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)

    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Set<FeatureGenomicMutationDetail> featureGenomicMutationDetailSet;

    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = false)
    private Set<SecondaryFeature> secondaryFeatureSet;


    public String getTransgenicSuffix() {
        return transgenicSuffix;
    }

    public void setTransgenicSuffix(String transgenicSuffix) {
        this.transgenicSuffix = transgenicSuffix;
    }

    public Set<PublicationAttribution> getPublications() {
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public Set<DataNote> getDataNotes() {
        return dataNotes;
    }

    public void setDataNotes(Set<DataNote> dataNotes) {
        this.dataNotes = dataNotes;
    }

    public SortedSet<DataNote> getSortedDataNotes() {
        return new TreeSet(this.getDataNotes());
    }

    public Set<FeatureNote> getExternalNotes() {
        return externalNotes;
    }

    public void setExternalNotes(Set<FeatureNote> externalNotes) {
        this.externalNotes = externalNotes;
    }

    public Boolean getKnownInsertionSite() {
        return isKnownInsertionSite;
    }

    public void setKnownInsertionSite(Boolean knownInsertionSite) {
        isKnownInsertionSite = knownInsertionSite;
    }

    public Boolean getDominantFeature() {
        return isDominantFeature;
    }

    public void setDominantFeature(Boolean dominantFeature) {
        isDominantFeature = dominantFeature;
    }

    public Boolean getUnspecifiedFeature() {
        return isUnspecifiedFeature;
    }

    public void setUnspecifiedFeature(Boolean unspecifiedFeature) {
        isUnspecifiedFeature = unspecifiedFeature;
    }


    public FeaturePrefix getFeaturePrefix() {
        return featurePrefix;
    }

    public void setFeaturePrefix(FeaturePrefix featurePrefix) {
        this.featurePrefix = featurePrefix;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public FeatureTypeEnum getType() {
        return type;
    }

    public void setType(FeatureTypeEnum type) {
        this.type = type;
    }

    @JsonView(View.FeatureAPI.class)
    public Set<FeatureSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<FeatureSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public Set<FeatureSource> getSources() {
        return sources;
    }

    public void setSources(Set<FeatureSource> sources) {
        this.sources = sources;
    }

    public FeatureAssay getFeatureAssay() {
        return featureAssay;
    }

    public void setFeatureAssay(FeatureAssay featureAssay) {
        this.featureAssay = featureAssay;
    }

    public Set<FeatureAlias> getAliases() {
        return aliases;
    }

    public void setAliases(Set<FeatureAlias> aliases) {
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getPublicComments() {
        return publicComments;
    }

    public void setPublicComments(String publicComments) {
        this.publicComments = publicComments;
    }

    public String getAbbreviationOrder() {
        return abbreviationOrder;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }
    public Date getFtrAssemblyInfoDate() {
        return ftrAssemblyInfoDate;
    }

    public void setFtrAssemblyInfoDate(Date ftrAssemblyInfoDate) {
        this.ftrAssemblyInfoDate = ftrAssemblyInfoDate;
    }


    public Set<FeatureMarkerRelationship> getFeatureMarkerRelations() {
        return featureMarkerRelations;
    }

    public void setFeatureMarkerRelations(Set<FeatureMarkerRelationship> featureMarkerRelations) {
        this.featureMarkerRelations = featureMarkerRelations;
    }


    public Set<GenotypeFeature> getGenotypeFeatures() {
        return genotypeFeatures;
    }

    public void setGenotypeFeatures(Set<GenotypeFeature> genotypeFeatures) {
        this.genotypeFeatures = genotypeFeatures;
    }


    public int compareTo(Object otherFeature) {
        return getAbbreviationOrder().compareTo(((Feature) otherFeature).getAbbreviationOrder());
    }

    public int getNumberOfRelatedGenotypes() {
        if (genotypeFeatures == null || genotypeFeatures.size() == 0) {
            return 0;
        }

        Set<Genotype> relatedGenotypes = new HashSet<>();
        for (GenotypeFeature genoFtr : genotypeFeatures) {
            if (genoFtr != null) {
                relatedGenotypes.add(genoFtr.getGenotype());
            }
        }
        return relatedGenotypes.size();
    }

    public Marker getSingleRelatedMarker() {
        if (featureMarkerRelations != null && featureMarkerRelations.size() == 1) {
            return featureMarkerRelations.iterator().next().getMarker();
        }
        return null;
    }

    public SortedSet<FeatureTranscriptMutationDetail> getFeatureTranscriptMutationDetailSet() {
        return featureTranscriptMutationDetailSet;
    }

    public void setFeatureTranscriptMutationDetailSet(SortedSet<FeatureTranscriptMutationDetail> mutationDetailSet) {
        this.featureTranscriptMutationDetailSet = mutationDetailSet;
    }

    public FeatureProteinMutationDetail getFeatureProteinMutationDetail() {
        if (CollectionUtils.isEmpty(featureProteinMutationDetailSet))
            return null;
        return featureProteinMutationDetailSet.stream().findAny().get();
    }

    public void setFeatureProteinMutationDetail(FeatureProteinMutationDetail featureProteinMutationDetail) {
        if (featureProteinMutationDetailSet == null)
            featureProteinMutationDetailSet = new HashSet<>();
        featureProteinMutationDetailSet.clear();
        featureProteinMutationDetailSet.add(featureProteinMutationDetail);
    }

    public Set<FeatureProteinMutationDetail> getFeatureProteinMutationDetailSet() {
        return featureProteinMutationDetailSet;
    }

    public void setFeatureProteinMutationDetailSet(Set<FeatureProteinMutationDetail> featureProteinMutationDetailSet) {
        this.featureProteinMutationDetailSet = featureProteinMutationDetailSet;
    }

    public FeatureDnaMutationDetail getFeatureDnaMutationDetail() {
        if (CollectionUtils.isEmpty(featureDnaMutationDetailSet))
            return null;
        return featureDnaMutationDetailSet.stream().findAny().get();
    }

    public void setFeatureDnaMutationDetail(FeatureDnaMutationDetail featureDnaMutationDetail) {
        if (featureDnaMutationDetailSet == null)
            featureDnaMutationDetailSet = new HashSet<>();
        featureDnaMutationDetailSet.clear();
        featureDnaMutationDetailSet.addAll(Collections.singleton(featureDnaMutationDetail));
    }

    public Set<FeatureDnaMutationDetail> getFeatureDnaMutationDetailSet() {
        return featureDnaMutationDetailSet;
    }

    public void setFeatureDnaMutationDetailSet(Set<FeatureDnaMutationDetail> featureDnaMutationDetailSet) {
        this.featureDnaMutationDetailSet = featureDnaMutationDetailSet;
    }


    public FeatureGenomicMutationDetail getFeatureGenomicMutationDetail() {
        if (CollectionUtils.isEmpty(featureGenomicMutationDetailSet))
            return null;
        return featureGenomicMutationDetailSet.stream().findAny().get();
    }

    public void setFeatureGenomicMutationDetail(FeatureGenomicMutationDetail FeatureGenomicMutationDetail) {
        if (featureGenomicMutationDetailSet == null)
            featureGenomicMutationDetailSet = new HashSet<>();
        featureGenomicMutationDetailSet.clear();
        featureGenomicMutationDetailSet.addAll(Collections.singleton(FeatureGenomicMutationDetail));
    }

    public Set<FeatureGenomicMutationDetail> getFeatureGenomicMutationDetailSet() {
        return featureGenomicMutationDetailSet;
    }


    public void setFeatureGenomicMutationDetailSet(Set<FeatureGenomicMutationDetail> FeatureGenomicMutationDetailSet) {
        this.featureGenomicMutationDetailSet = FeatureGenomicMutationDetailSet;
    }

    public Genotype getSingleRelatedGenotype() {
        if (genotypeFeatures != null && genotypeFeatures.size() == 1) {
            return genotypeFeatures.iterator().next().getGenotype();
        }
        return null;
    }

    public Set<FeatureMarkerRelationship> getConstructs() {
        if (!type.isTransgenic()) {
            return null;
        }
        if (featureMarkerRelations == null) {
            return new TreeSet<>();
        }
        SortedSet<FeatureMarkerRelationship> constructs = new TreeSet<>();
        for (FeatureMarkerRelationship ftrmrkrRelation : featureMarkerRelations) {
            if (ftrmrkrRelation != null) {
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString())
                        || ftrmrkrRelation.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())) {
                    constructs.add(ftrmrkrRelation);
                }
            }
        }
        return constructs;
    }

    @Override
    public String getEntityType() {
        return MUTANT;
    }

    @Override
    public String getEntityName() {
        return getAbbreviation();
    }

    public Set<FeatureDBLink> getDbLinks() {
        return dbLinks;
    }

    public void setDbLinks(Set<FeatureDBLink> dbLinks) {
        this.dbLinks = dbLinks;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Feature");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", lineNumber='").append(lineNumber).append('\'');
        sb.append(", labPrefix=").append(featurePrefix);
        sb.append(", abbreviation='").append(abbreviation).append('\'');
        sb.append(", transgenicSuffix='").append(transgenicSuffix).append('\'');
        sb.append(", isKnownInsertionSite=").append(isKnownInsertionSite);
        sb.append(", isDominantFeature=").append(isDominantFeature);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

    @JsonView(View.FeatureAPI.class)
    public SortedSet<Marker> getAffectedGenes() {
        SortedSet<Marker> affectedGenes = new TreeSet<>();
        for (FeatureMarkerRelationship featureMarkerRelationship : featureMarkerRelations) {
            if (featureMarkerRelationship.isMarkerIsGene() && featureMarkerRelationship.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                affectedGenes.add(featureMarkerRelationship.getMarker());
            }
        }
        return affectedGenes;
    }


    public SortedSet<FeatureMarkerRelationship> getAffectedGenesReln() {
        SortedSet<FeatureMarkerRelationship> affectedGenesReln = new TreeSet<>();
        for (FeatureMarkerRelationship featureMarkerRelationship : featureMarkerRelations) {
            if (featureMarkerRelationship.isMarkerIsGene() && featureMarkerRelationship.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                affectedGenesReln.add(featureMarkerRelationship);
            }
        }
        return affectedGenesReln;
    }

    @JsonView(View.FeatureAPI.class)
    public SortedSet<Marker> getTgConstructs() {
        SortedSet<Marker> tgConstructs = new TreeSet<>();
        for (FeatureMarkerRelationship ftrmarkrel : featureMarkerRelations) {
            if (ftrmarkrel.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE.toString())
                    || ftrmarkrel.getFeatureMarkerRelationshipType().getName().equals(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString())
            ) {

                tgConstructs.add(ftrmarkrel.getMarker());
            }
        }
        return tgConstructs;
    }

    public boolean isAllZfishbook() {
        if (dbLinks == null || dbLinks.isEmpty()) {
            return false;
        }

        int countZfishbook = 0;
        for (FeatureDBLink dblink : dbLinks) {
            if (dblink.getReferenceDatabase().getForeignDB().getDisplayName() != null && dblink.getReferenceDatabase().getForeignDB().getDisplayName().equals("zfishbook")) {
                countZfishbook++;
            }
        }

        if (countZfishbook == dbLinks.size()) {
            return true;
        }

        return false;
    }

    public boolean isNoZfishbook() {
        if (dbLinks == null || dbLinks.isEmpty()) {
            return true;
        }

        for (FeatureDBLink dblink : dbLinks) {
            if (dblink.getReferenceDatabase().getForeignDB().getDisplayName() != null && dblink.getReferenceDatabase().getForeignDB().getDisplayName().equals("zfishbook")) {
                return false;
            }
        }

        return true;
    }

    public boolean isAllZmp() {
        if (dbLinks == null || dbLinks.isEmpty()) {
            return false;
        }

        int countZmp = 0;
        for (FeatureDBLink dblink : dbLinks) {
            if (dblink.getReferenceDatabase().getForeignDB().getDisplayName() != null && dblink.getReferenceDatabase().getForeignDB().getDisplayName().equals("ZMP")) {
                countZmp++;
            }
        }

        if (countZmp == dbLinks.size()) {
            return true;
        }

        return false;
    }

    public boolean isAllCrezoo() {
        if (dbLinks == null || dbLinks.isEmpty()) {
            return false;
        }

        int countCreZoo = 0;
        for (FeatureDBLink dblink : dbLinks) {
            if (dblink.getReferenceDatabase().getForeignDB().getDisplayName() != null && dblink.getReferenceDatabase().getForeignDB().getDisplayName().equals("CreZoo")) {
                countCreZoo++;
            }
        }

        if (countCreZoo == dbLinks.size()) {
            return true;
        }

        return false;
    }

    public boolean isNoZmp() {
        if (dbLinks == null || dbLinks.isEmpty()) {
            return true;
        }

        for (FeatureDBLink dblink : dbLinks) {
            if (dblink.getReferenceDatabase().getForeignDB().getDisplayName() != null && dblink.getReferenceDatabase().getForeignDB().getDisplayName().equals("zfishbook")) {
                return false;
            }
        }

        return true;
    }

    public boolean isNoCrezoo() {
        if (dbLinks == null || dbLinks.isEmpty()) {
            return true;
        }

        for (FeatureDBLink dblink : dbLinks) {
            if (dblink.getReferenceDatabase().getForeignDB().getDisplayName() != null && dblink.getReferenceDatabase().getForeignDB().getDisplayName().equals("CreZoo")) {
                return false;
            }
        }

        return true;
    }

    public Marker getAllelicGene() {
        if (featureMarkerRelations == null) {
            return null;
        }
        for (FeatureMarkerRelationship relationship : featureMarkerRelations) {
            if (relationship.getType().equals(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF)) {
                return relationship.getMarker();
            }
        }
        return null;
    }

    public boolean isMultiAllelic() {
        if (featureMarkerRelations == null) {
            return false;
        }
        Set<Marker> allelicMarker = featureMarkerRelations.stream()
                .filter(relationship -> relationship.getType().equals(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF))
                .map(FeatureMarkerRelationship::getMarker)
                .collect(Collectors.toSet());
        return allelicMarker.size() > 1;
    }

    @JsonView(View.FeatureAPI.class)
    public String getGeneLocalizationStatement() {
        return mutationDetailsConversionService.geneLocalizationStatement(getFeatureDnaMutationDetail());
    }

    @JsonView(View.FeatureAPI.class)
    public String getTranscriptConsequenceStatement() {
        return mutationDetailsConversionService.getTranscriptMutationStatement(this);
    }

    public String getDisplayAbbreviation() {
        if (abbreviation.endsWith("_" + UNRECOGNIZED)) {
            return UNRECOGNIZED;
        }
        if (abbreviation.endsWith("_" + UNSPECIFIED)) {
            return UNSPECIFIED;
        }
        return abbreviation;

    }

    public void addExternalNote(FeatureNote note) {
        if (externalNotes == null) {
            externalNotes = new HashSet<>();
        }
        externalNotes.add(note);
    }

    public void addMutationDetailTranscript(FeatureTranscriptMutationDetail detail) {
        if (featureTranscriptMutationDetailSet == null)
            featureTranscriptMutationDetailSet = new TreeSet<>();
        featureTranscriptMutationDetailSet.add(detail);
    }

    public Set<SecondaryFeature> getSecondaryFeatureSet() {
        return secondaryFeatureSet;
    }

    public void setSecondaryFeatureSet(Set<SecondaryFeature> secondaryFeatureSet) {
        this.secondaryFeatureSet = secondaryFeatureSet;
    }

    public boolean isSingleAlleleOfMarker(Marker gene) {
        if (CollectionUtils.isEmpty(featureMarkerRelations))
            return false;
        for (FeatureMarkerRelationship featureMarkerRelationship : featureMarkerRelations)
            if (featureMarkerRelationship.getType().equals(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF) &&
                    featureMarkerRelationship.getMarker().equals(gene))
                return true;
        return false;
    }

    public boolean hasAlleleOfRelationship() {
        if (CollectionUtils.isEmpty(featureMarkerRelations))
            return false;
        for (FeatureMarkerRelationship featureMarkerRelationship : featureMarkerRelations)
            if (featureMarkerRelationship.getType().equals(FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF))
                return true;
        return false;
    }

    public boolean isReadyForCuration() {
        if (type.equals(FeatureTypeEnum.TRANSGENIC_INSERTION)) {
            return featureMarkerRelations.stream()
                    .filter(relationship -> relationship.getMarker().isConstruct())
                    .findAny()
                    .orElse(null) != null;
        }
        return true;
    }

    public boolean isInnocuousOnlyTG() {
        if (CollectionUtils.isEmpty(featureMarkerRelations))
            return false;
        if (!type.equals(FeatureTypeEnum.TRANSGENIC_INSERTION))
            return false;
        List<FeatureMarkerRelationshipTypeEnum> relationships = featureMarkerRelations.stream()
                .map(FeatureMarkerRelationship::getType)
                .collect(Collectors.toList());
        if (relationships.contains(FeatureMarkerRelationshipTypeEnum.CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE))
            return false;
        if (relationships.contains(FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE))
            return true;
        return false;
    }

    public String getDisplayType() {
        if (type.equals(FeatureTypeEnum.TRANSGENIC_INSERTION) && hasAlleleOfRelationship())
            return "Allele caused by " + type.getTypeDisplay();
        return type.getTypeDisplay();
    }

    public Date getFtrEntryDate() {
        return ftrEntryDate;
    }

    public void setFtrEntryDate(Date ftrEntryDate) {
        this.ftrEntryDate = ftrEntryDate;
    }
}
