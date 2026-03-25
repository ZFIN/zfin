
package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SortNatural;
import org.zfin.ExternalNote;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.expression.Figure;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.*;
import org.zfin.mapping.MappedMarkerImpl;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.orthology.Ortholog;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.gff.Assembly;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain model for the abstract marker object, which can be a gene, EST, CDNA, ...
 * ToDo: needs more modelling...
 */
@Entity
@Table(name = "marker")
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicUpdate
@Setter
@Getter
public class Marker extends SequenceFeature implements Serializable, Comparable, EntityAlias, EntityNotes, EntityID, ZdbID {

    public static final String WITHDRAWN = "WITHDRAWN:";
    private static Logger LOG = LogManager.getLogger(Marker.class);

    @Column(name = "mrkr_abbrev", nullable = false)
    private String abbreviation;

    @Column(name = "mrkr_abbrev_order")
    private String abbreviationOrder;

    @Transient
    private Set<ExpressionExperiment2> expressionExperiments;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "recattrib_data_zdb_id")
    @JsonView(View.SequenceTargetingReagentAPI.class)
    private Set<PublicationAttribution> publications;

    @Transient
    private HashMap<String, List<Publication>> pubsPerAnatomy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "construct_figure",
            joinColumns = @JoinColumn(name = "consfig_construct_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "consfig_fig_zdb_id"))
    private Set<Figure> figures;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "genedom_family_member",
            joinColumns = @JoinColumn(name = "gfammem_mrkr_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "gfammem_gfam_name"))
    private Set<MarkerFamilyName> familyName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "fpProtein_efg",
            joinColumns = @JoinColumn(name = "fe_mrkr_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "fe_fl_protein_id"))
    private Set<FluorescentProtein> fluorescentProteinEfgs;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "fpProtein_construct",
            joinColumns = @JoinColumn(name = "fc_mrkr_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "fc_fl_protein_id"))
    private Set<FluorescentProtein> fluorescentProteinConstructs;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "fm_mrkr_zdb_id")
    private Set<FluorescentMarker> fluorescentMarkers;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ortho_zdb_id")
    private Set<Ortholog> orthologs;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "mrel_mrkr_1_zdb_id")
    protected Set<MarkerRelationship> firstMarkerRelationships;    //  where this marker = "mrel_mrkr_1_zdb_id" in mrel

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "mrel_mrkr_2_zdb_id")
    private Set<MarkerRelationship> secondMarkerRelationships;   //  where this marker = "mrel_mrkr_2_zdb_id" in mrel

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "fmrel_mrkr_zdb_id")
    private Set<FeatureMarkerRelationship> featureMarkerRelationships;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mrkr_type")
    private MarkerType markerType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "mhist_mrkr_zdb_id")
    @SortNatural
    private Set<MarkerHistory> markerHistory;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "marker_id")
    private Set<MappedMarkerImpl> directPanelMappings;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mrkr_owner")
    private Person owner;

    @Column(name = "mrkr_comments")
    private String publicComments;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dblink_linked_recid")
    private Set<MarkerDBLink> dbLinks;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dalias_data_zdb_id")
    @org.hibernate.annotations.SQLOrder("dalias_alias_lower")
    private Set<MarkerAlias> aliases;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dnote_data_zdb_id")
    @OrderBy("date desc")
    private Set<DataNote> dataNotes;

    @OneToMany
    @JoinColumn(name = "idsup_data_zdb_id", updatable = false)
    private Set<MarkerSupplier> suppliers;

    @Transient
    private String chromosome;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "mrkrgoev_mrkr_zdb_id")
    private Set<MarkerGoTermEvidence> goTermEvidence;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "zrepld_new_zdb_id")
    private Set<SecondaryMarker> secondaryMarkerSet;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "marker_assembly",
            joinColumns = @JoinColumn(name = "ma_mrkr_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "ma_a_pk_id"))
    private Set<Assembly> assemblies;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "marker_annotation_status",
            joinColumns = @JoinColumn(name = "mas_mrkr_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "mas_vt_pk_id"))
    private Set<VocabularyTerm> annotationStatusTerms;

    // cached attribute
    @Transient
    private transient List<Marker> markers;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "extnote_data_zdb_id")
    private Set<OrthologyNote> orthologyNotes;

    public String getAbbreviationOrder() {
        return abbreviationOrder;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getPublicComments() {
        return publicComments;
    }

    @Override
    public Set<? extends ExternalNote> getExternalNotes() {
        return null;
    }

    public void setPublicComments(String comments) {
        this.publicComments = comments;
    }

    public Set<MarkerDBLink> getDbLinks() {
        return dbLinks;
    }

    public void setDbLinks(Set<MarkerDBLink> dbLinks) {
        this.dbLinks = dbLinks;
    }

    public Set<MarkerAlias> getAliases() {
        if (aliases == null || aliases.size() == 0)
            return null;
        return aliases;
    }

    @JsonView({View.ExpressedGeneAPI.class, View.API.class, View.ExpressionPublicationUI.class, View.PublicationUI.class})
    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAliases(Set<MarkerAlias> aliases) {
        this.aliases = aliases;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }

    public Set<PublicationAttribution> getPublications() {
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    @Override
    public String getEntityType() {
        return getType().toString();
    }

    @Override
    public String getEntityName() {
        return name;
    }

    /**
     * Note that a single publication can be used in multiple expressionExperiments!
     * s
     *
     * @return number
     */
    public int getNumberOfPublications() {
        if (expressionExperiments == null)
            return 0;
        return getPublications().size();
    }

    public Set<OrthologyNote> getOrthologyNotes() {
        return orthologyNotes;
    }

    public void setOrthologyNotes(Set<OrthologyNote> orthologyNotes) {
        this.orthologyNotes = orthologyNotes;
    }

    public OrthologyNote getOrthologyNote() {
        if (orthologyNotes == null || orthologyNotes.size() == 0)
            return null;

        if (orthologyNotes.size() > 1) {
            String message = "More than one Orthology note found. This is not allowed!";
            LOG.error(message);
        }

        return orthologyNotes.iterator().next();
    }

    /**
     * Retrieve all related markers, no dublicates.
     *
     * @return List of marker objects
     */
    public List<Marker> getAllRelatedMarker() {
        Set<MarkerRelationship> relationshipsFirst = getFirstMarkerRelationships();
        for (MarkerRelationship relationship : relationshipsFirst) {
            if (markers == null)
                markers = new ArrayList<>();
            if (!markers.contains(relationship.getSecondMarker()))
                markers.add(relationship.getSecondMarker());
        }
        Set<MarkerRelationship> relationshipsSecond = getSecondMarkerRelationships();
        for (MarkerRelationship relationship : relationshipsSecond) {
            if (markers == null)
                markers = new ArrayList<>();
            if (!markers.contains(relationship.getFirstMarker()))
                markers.add(relationship.getFirstMarker());
        }
        if (markers != null)
            Collections.sort(markers);
        return markers;
    }


    public GenericTerm getSoTerm() {
        return MarkerService.getSoTerm(this);
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public Set<Ortholog> getOrthologs() {
        return orthologs;
    }

    public void setOrthologs(Set<Ortholog> orthologs) {
        this.orthologs = orthologs;
    }

    public Set<MarkerRelationship> getFirstMarkerRelationships() {
        return firstMarkerRelationships;
    }

    public void setFirstMarkerRelationships(Set<MarkerRelationship> firstMarkerRelationships) {
        this.firstMarkerRelationships = firstMarkerRelationships;
    }

    public Set<MarkerRelationship> getSecondMarkerRelationships() {
        return secondMarkerRelationships;
    }

    public void setSecondMarkerRelationships(Set<MarkerRelationship> secondMarkerRelationships) {
        this.secondMarkerRelationships = secondMarkerRelationships;
    }

    public Set<FeatureMarkerRelationship> getFeatureMarkerRelationships() {
        return featureMarkerRelationships;
    }

    public void setFeatureMarkerRelationships(Set<FeatureMarkerRelationship> featureMarkerRelationships) {
        this.featureMarkerRelationships = featureMarkerRelationships;
    }

    @JsonView(View.API.class)
    public Type getType() {
        if (markerType == null)
            return null;

        return markerType.getType();
    }

    public boolean isInTypeGroup(TypeGroup typeGroup) {
        return markerType.getTypeGroups().contains(typeGroup);
    }

    public boolean isGenedom() {
        return isInTypeGroup(TypeGroup.GENEDOM_AND_NTR);
    }

    public boolean isNontranscribed() {
        return isInTypeGroup(TypeGroup.NONTSCRBD_REGION);
    }

    public MarkerType getMarkerType() {
        if (markerType == null)
            return null;
        return markerType;
    }

    public void setMarkerType(MarkerType markerType) {
        this.markerType = markerType;
    }


    public Set<MarkerHistory> getMarkerHistory() {
        return markerHistory;
    }

    public void setMarkerHistory(Set<MarkerHistory> markerHistory) {
        this.markerHistory = markerHistory;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MARKER");
        sb.append("\n");
        sb.append("zdbID: ").append(zdbID);
        sb.append("\n");
        sb.append("symbol: ").append(abbreviation);
        sb.append("\n");
        sb.append("name: ").append(name);
        sb.append("\n");
        sb.append("type: ").append(markerType);
        sb.append("\n");
        return sb.toString();
    }

    /**
     * equality check, only using zdb id for now
     *
     * @param otherMarker marker to check equality against
     * @return equality boolean
     */
    public boolean equals(Object otherMarker) {
        if (!(otherMarker instanceof Marker om))
            return false;

        return getZdbID().equals(om.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

    public int compareTo(Object otherMarker) {
        if (otherMarker == null || ((Marker) otherMarker).getAbbreviationOrder() == null) {
            return 1;
        }
        if (getAbbreviationOrder() == null) {
            return -1;
        }
        return getAbbreviationOrder().compareTo(((Marker) otherMarker).getAbbreviationOrder());
    }

    public Set<MappedMarkerImpl> getDirectPanelMappings() {
        return directPanelMappings;
    }

    public void setDirectPanelMappings(Set<MappedMarkerImpl> directPanelMappings) {
        this.directPanelMappings = directPanelMappings;
    }

    public boolean hasFirstMarkerRelationships(Marker markerToMergeInto) {
        for (MarkerRelationship markerRelationship : getSecondMarkerRelationships()) {
            if (markerRelationship.getFirstMarker().equals(markerToMergeInto)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSecondMarkerRelationships(Marker markerToMergeInto) {
        for (MarkerRelationship markerRelationship : getFirstMarkerRelationships()) {
            if (markerRelationship.getSecondMarker().equals(markerToMergeInto)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSupplier(MarkerSupplier markerSupplier) {
        for (MarkerSupplier aMarkerSupplier : getSuppliers()) {
            if (aMarkerSupplier.getOrganization().getZdbID().equals(markerSupplier.getOrganization().getZdbID())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPublicationAttribution(PublicationAttribution publicationAttribution) {
        for (PublicationAttribution aPublicationAttribution : getPublications()) {
            if (aPublicationAttribution.getPublication().getZdbID().equals(publicationAttribution.getPublication().getZdbID())) {
                return true;
            }
        }
        return false;
    }

    public MarkerAlias getAlias(String aliasString) {
        if (CollectionUtils.isEmpty(getAliases())) return null;

        for (MarkerAlias aMarkerAlias : getAliases()) {
            if (aMarkerAlias.getAlias().equalsIgnoreCase(aliasString)) {
                return aMarkerAlias;
            }
        }
        return null;
    }

    public boolean isConstruct() {
        return getType().getConstructs().contains(getType());
    }

    public boolean isAntibody() {
        return getType().isAntibody();
    }

    public enum Type {
        ATB("ATB"),
        BAC("BAC"),
        BAC_END("BAC_END"),
        CDNA("CDNA"),
        EFG("EFG"),
        EST("EST"),
        FOSMID("FOSMID"),
        ETCONSTRCT("ETCONSTRCT"),
        GENE("GENE"),
        GENEFAMILY("GENEFAMILY"),
        GENEP("GENEP"),
        GTCONSTRCT("GTCONSTRCT"),
        MRPHLNO("MRPHLNO", "MO"),
        MUTANT("MUTANT"),
        PAC("PAC"),
        PAC_END("PAC_END"),
        PTCONSTRCT("PTCONSTRCT"),
        RAPD("RAPD"),
        EREGION("EREGION"),
        SNP("SNP"),
        SSLP("SSLP"),
        STS("STS"),
        TGCONSTRCT("TGCONSTRCT"),
        TSCRIPT("TSCRIPT"),
        TALEN("TALEN"),
        CRISPR("CRISPR"),
        CNE("CNE"),
        LNCRNAG("LNCRNAG"),
        LINCRNAG("LINCRNAG"),
        MIRNAG("MIRNAG"),
        PIRNAG("PIRNAG"),
        SCRNAG("SCRNAG"),
        SNORNAG("SNORNAG"),
        TRNAG("TRNAG"),
        RRNAG("RRNAG"),
        NCRNAG("NCRNAG"),
        HISTBS("HISTBS"),
        PROTBS("PROTBS"),
        NCCR("NCCR"),
        BR("BR"),
        BINDSITE("BINDSITE"),
        LIGANDBS("LIGANDBS"),
        TFBS("TFBS"),
        EBS("EBS"),
        NCBS("NCBS"),
        EMR("EMR"),
        HMR("HMR"),
        MDNAB("MDNAB"),
        RR("RR"),
        TRR("TRR"),
        TLNRR("TLNRR"),
        PROMOTER("PROMOTER"),
        ENHANCER("ENHANCER"),
        LCR("LCR"),
        NUCMO("NUCMO"),
        DNAMO("DNAMO"),
        RNAMO("RNAMO"),
        CPGISLAND("CPGISLAND"),
        SRPRNAG("SRPRNAG");

        private final String value;
        private final String prefix;

        Type(String type) {
            this.value = type;
            this.prefix = type;
        }

        private Type(String type, String prefix) {
            this.value = type;
            this.prefix = prefix;
        }

        public String toString() {
            return this.value;
        }

        public String getPrefix() {
            return this.prefix;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }

        public static Type getTypeByPrefix(String prefix) {
            for (Type t : values()) {
                if (t.getPrefix().equals(prefix))
                    return t;
            }
            throw new RuntimeException("No run type of prefix " + prefix + " found.");
        }

        public static boolean isMarkerType(String type) {
            if (type == null)
                return false;
            for (Type markerType : values())
                if (markerType.value.equals(type))
                    return true;
            return false;
        }

        public boolean isGeneOrGenep() {
            for (Type markerType : values())
                if (markerType.equals(Type.GENE) || markerType.equals(Type.GENEP))
                    return true;
            return false;
        }

        public boolean isAntibody() {
            return this.equals(Type.ATB);
        }

        public List<Type> getConstructs() {
            return Arrays.stream(values())
                .filter(type -> type.value.contains("CONSTRCT"))
                .collect(Collectors.toList());
        }
    }

    public enum TypeGroup {
        ABBREV_EQ_NAME("ABBREV_EQ_NAME"),
        ATB("ATB"),
        BAC("BAC"),
        BAC_END("BAC_END"),
        CAN_BE_PROMOTER("CAN_BE_PROMOTER"),
        CAN_HAVE_MRPHLN("CAN_HAVE_MRPHLN"),
        CDNA("CDNA"),
        CDNA_AND_EST("CDNA_AND_EST"),
        CLONE("CLONE"),
        CLONEDOM("CLONEDOM"),
        CONSTRUCT("CONSTRUCT"),
        CRISPR("CRISPR"),
        EFG("EFG"),
        EST("EST"),
        FEATURE("FEATURE"),
        FOSMID("FOSMID"),
        GENE("GENE"),
        GENEDOM("GENEDOM"),
        GENEDOM_AND_EFG("GENEDOM_AND_EFG"),
        GENEP("GENEP"),
        KNOCKDOWN_REAGENT("KNOCKDOWN_REAGENT"),
        MRPHLNO("MRPHLNO"),
        MUTANT("MUTANT"),
        PAC("PAC"),
        PAC_END("PAC_END"),
        POLYMORPH("POLYMORPH"),
        RAPD("RAPD"),
        SEARCH_MK("SEARCH_MK"),
        SEARCH_MKSEG("SEARCH_MKSEG"),
        SEARCH_SEG("SEARCH_SEG"),
        SMALLSEG("SMALLSEG"),
        SMALLSEG_NO_ESTCDNA("SMALLSEG_NO_ESTCDNA"),
        SSLP("SSLP"),
        STS("STS"),
        TALEN("TALEN"),
        TGCONSTRUCT("TGCONSTRUCT"),
        TRANSCRIPT("TRANSCRIPT"),
        DEFICIENCY_TLOC_MARK("DEFICIENCY_TLOC_MARK"),
        GENEDOM_EFG_EREGION("GENEDOM_EFG_EREGION"),
        GENEDOM_EFG_EREGION_K("GENEDOM_EFG_EREGION_K"),
        SRPRNAG("SRPRNAG"),
        LNCRNAG("LNCRNAG"),
        LINCRNAG("LINCRNAG"),
        MIRNAG("MIRNAG"),
        PIRNAG("PIRNAG"),
        SCRNAG("SCRNAG"),
        SNORNAG("SNORNAG"),
        TRNAG("TRNAG"),
        RRNAG("RRNAG"),
        NCRNAG("NCRNAG"),
        HISTBS("HISTBS"),
        PROTBS("PROTBS"),
        NCCR("NCCR"),
        BR("BR"),
        BINDSITE("BINDSITE"),
        LIGANDBS("LIGANDBS"),
        TFBS("TFBS"),
        EBS("EBS"),
        NCBS("NCBS"),
        EMR("EMR"),
        HMR("HMR"),
        MDNAB("MDNAB"),
        RR("RR"),
        TRR("TRR"),
        TLNRR("TLNRR"),
        PROMOTER("PROMOTER"),
        ENHANCER("ENHANCER"),
        LCR("LCR"),
        NUCMO("NUCMO"),
        DNAMO("DNAMO"),
        RNAMO("RNAMO"),
        CPGISLAND("CPGISLAND"),
        ENGINEERED_REGION("ENGINEERED_REGION"),
        GENEDOM_PROD_PROTEIN("GENEDOM_PROD_PROTEIN"),
        NONTSCRBD_REGION("NONTSCRBD_REGION"),
        GENEDOM_AND_NTR("GENEDOM_AND_NTR"),
        CONSTRUCT_COMPONENTS("CONSTRUCT_COMPONENTS"),
        SEARCHABLE_CDNA_EST("SEARCHABLE_CDNA_EST", "cDNA/EST"),
        SEARCHABLE_GENOMIC_CLONE("SEARCHABLE_GENOMIC_CLONE", "Genomic Clone"),
        SEARCHABLE_CONSTRUCT("SEARCHABLE_CONSTRUCT", "Construct"),
        SEARCHABLE_EFG("SEARCHABLE_EFG", "Foreign Gene"),
        SEARCHABLE_MAPPING_MARKER("SEARCHABLE_MAPPING_MARKER", "Mapping Marker"),
        SEARCHABLE_NON_PROTEIN_CODING_GENE("SEARCHABLE_NON_PROTEIN_CODING_GENE", "Non-Protein Coding Gene"),
        SEARCHABLE_NON_TRANSCRIBED_REGION("SEARCHABLE_NTR", "Non-Transcribed Region"),
        SEARCHABLE_PROTEIN_CODING_GENE("SEARCHABLE_PROTEIN_CODING_GENE", "Protein Coding Gene"),
        SEARCHABLE_STR("SEARCHABLE_STR", "Sequence Targeting Reagent"),
        SEARCHABLE_TRANSCRIPT("SEARCHABLE_TRANSCRIPT", "Transcript"),
        RNAGENE("RNAGENE");

        private final String value;
        private final String displayName;

        private TypeGroup(String type) {
            this.value = type;
            this.displayName = null;
        }

        private TypeGroup(String type, String displayName) {
            this.value = type;
            this.displayName = displayName;
        }

        public String toString() {
            return this.value;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public static TypeGroup getType(String type) {
            for (TypeGroup t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }


    }

    public TreeSet<String> getChromosomeLocations() {
        TreeSet<String> lgSet = RepositoryFactory.getLinkageRepository().getChromosomeLocations(this);
        lgSet.remove("0");
        return lgSet;
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

    /**
     * Only be used because family names are stored in a separate tabel.
     * Todo: Better to have subclass Gene
     *
     * @return set of family names
     */
    public Set<MarkerFamilyName> getFamilyName() {
        return familyName;
    }

    /**
     * Only be used because family names are stored in a separate tabel.
     * Todo: Better to have subclass Gene
     *
     * @param familyName family names
     */
    public void setFamilyName(Set<MarkerFamilyName> familyName) {
        this.familyName = familyName;
    }

    /**
     * Return a sorted set of suppliers.
     *
     * @return set of Suppliers
     */
    public Set<MarkerSupplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<MarkerSupplier> suppliers) {
        this.suppliers = suppliers;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Set<MarkerGoTermEvidence> getGoTermEvidence() {
        return goTermEvidence;
    }

    public void setGoTermEvidence(Set<MarkerGoTermEvidence> goTermEvidence) {
        this.goTermEvidence = goTermEvidence;
    }

    public Set<SecondaryMarker> getSecondaryMarkerSet() {
        return secondaryMarkerSet;
    }

    public void setSecondaryMarkerSet(Set<SecondaryMarker> secondaryMarkerSet) {
        this.secondaryMarkerSet = secondaryMarkerSet;
    }

    public Set<FluorescentMarker> getFluorescentMarkers() {
        if (fluorescentMarkers == null)
            return null;
        return fluorescentMarkers;
    }

    @JsonView(View.SequenceAPI.class)
    public Assembly getLatestAssembly() {
        if (CollectionUtils.isEmpty(assemblies)) {
            return null;
        }
        return assemblies.stream().min(Comparator.comparing(Assembly::getOrder)).get();
    }

    public List<Assembly> getAllAssemblies() {
        return assemblies.stream().sorted(Comparator.comparing(Assembly::getOrder)).toList();
    }

    @JsonView(View.SequenceAPI.class)
    public String getAnnotationStatus() {
        if (CollectionUtils.isEmpty(annotationStatusTerms)) {
            return "Unknown";
        }
        return annotationStatusTerms.stream().min(Comparator.comparing(VocabularyTerm::getName)).get().getName();
    }
}
