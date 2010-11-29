package org.zfin.marker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityAlias;
import org.zfin.infrastructure.EntityNotes;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mapping.MappedMarker;
import org.zfin.orthology.Orthologue;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.io.Serializable;
import java.util.*;

/**
 * Domain model for the abstract marker object, which can be a gene, EST, CDNA, ...
 * ToDo: needs more modelling...
 */
public class Marker implements Serializable, Comparable, EntityAlias, EntityNotes {

    public static final String WITHDRAWN = "WITHDRAWN:";
    private static Logger LOG = Logger.getLogger(Marker.class);

    private String zdbID;
    private String name;
    private String abbreviation;
    private String abbreviationOrder;
    private Set<ExpressionExperiment> probeExpressionExperiments;
    private Set<ExpressionExperiment> expressionExperiments;
    private Set<PublicationAttribution> publications;
    private HashMap<String, List<Publication>> pubsPerAnatomy;
    private Set<Figure> figures;
    private Set<MarkerFamilyName> familyName;
    private Set<Orthologue> orthologues;
    private Set<MarkerRelationship> firstMarkerRelationships;    //  where this marker = "mrel_mrkr_1_zdb_id" in mrel
    private Set<MarkerRelationship> secondMarkerRelationships;   //  where this marker = "mrel_mrkr_2_zdb_id" in mrel
    private MarkerType markerType;
    private Set<MarkerHistory> markerHistory;
    private Set<MappedMarker> directPanelMappings;
    private Person owner;
    private String publicComments;
    private Set<MarkerDBLink> dbLinks;
    private Set<MarkerAlias> aliases;
    private Set<DataNote> dataNotes;
    private Set<MarkerSupplier> suppliers;

    // cashed attribute
    private transient List<Marker> markers;
    private Set<OrthologyNote> orthologyNotes;


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

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

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

    public void setAliases(Set<MarkerAlias> aliases) {
        this.aliases = aliases;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }

    public Set<ExpressionExperiment> getProbeExpressionExperiments() {
        return probeExpressionExperiments;
    }

    public void setProbeExpressionExperiments(Set<ExpressionExperiment> probeExpressionExperiments) {
        this.probeExpressionExperiments = probeExpressionExperiments;
    }

    public Set<ExpressionExperiment> getExpressionExperiments() {
        return expressionExperiments;
    }

    public void setExpressionExperiments(Set<ExpressionExperiment> expressionExperiments) {
        this.expressionExperiments = expressionExperiments;
    }

    public Set<PublicationAttribution> getPublications() {
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    /**
     * obtain a list of publications that are expressed in this gene and
     * the given anatomical structure.
     *
     * @param aoZdbID ZdbID of anatomy object.
     * @return List of publications.
     */
    public List<Publication> getPublications(String aoZdbID) {
        List<Publication> pubs;
/*
        if (pubsPerAnatomy == null)
            pubsPerAnatomy = new HashMap<String, List<Publication>>();
        pubs = pubsPerAnatomy.get(aoZdbID);
        if (pubs != null)
            return pubs;

*/
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        pubs = pr.getExpressedGenePublications(zdbID, aoZdbID);
//        pubsPerAnatomy.put(aoZdbID, pubs);
        return pubs;
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
            String message = "More than one Ortholgoy notes found. This is not allowed!";
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
                markers = new ArrayList<Marker>();
            if (!markers.contains(relationship.getSecondMarker()))
                markers.add(relationship.getSecondMarker());
        }
        Set<MarkerRelationship> relationshipsSecond = getSecondMarkerRelationships();
        for (MarkerRelationship relationship : relationshipsSecond) {
            if (markers == null)
                markers = new ArrayList<Marker>();
            if (!markers.contains(relationship.getFirstMarker()))
                markers.add(relationship.getFirstMarker());
        }
        if (markers != null)
            Collections.sort(markers);
        return markers;
    }


    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public Set<Orthologue> getOrthologues() {
        return orthologues;
    }

    public void setOrthologues(Set<Orthologue> orthologues) {
        this.orthologues = orthologues;
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

    public Type getType() {
        if (markerType == null)
            return null;

        return markerType.getType();
    }

    public boolean isInTypeGroup(TypeGroup typeGroup) {
        return markerType.getTypeGroups().contains(typeGroup);
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
        sb.append("name: ").append(name);
        sb.append("\n");
        sb.append("symbol: ").append(abbreviation);
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
        if (!(otherMarker instanceof Marker))
            return false;

        Marker om = (Marker) otherMarker;
        return getZdbID().equals(om.getZdbID());
    }

    public int hashCode() {
        return zdbID.hashCode();
    }

    public int compareTo(Object otherMarker) {
		if (otherMarker == null)
            return 1;
        if (((Marker)otherMarker).getAbbreviationOrder() == null)
            return 1;
        return getAbbreviationOrder().compareTo(((Marker) otherMarker).getAbbreviationOrder());
    }

    public Set<MappedMarker> getDirectPanelMappings() {
        return directPanelMappings;
    }

    public void setDirectPanelMappings(Set<MappedMarker> directPanelMappings) {
        this.directPanelMappings = directPanelMappings;
    }

    public boolean hasFirstMarkerRelationships(Marker markerToMergeInto) {
        for(MarkerRelationship markerRelationship: getSecondMarkerRelationships()){
            if(markerRelationship.getFirstMarker().equals(markerToMergeInto)){
                return true ;
            }
        }
        return false ;
    }

    public boolean hasSecondMarkerRelationships(Marker markerToMergeInto) {
        for(MarkerRelationship markerRelationship: getFirstMarkerRelationships()){
            if(markerRelationship.getSecondMarker().equals(markerToMergeInto)){
                return true ;
            }
        }
        return false ;
    }

    public boolean hasSupplier(MarkerSupplier markerSupplier) {
        for(MarkerSupplier aMarkerSupplier: getSuppliers()){
           if(aMarkerSupplier.getOrganization().getZdbID().equals(markerSupplier.getOrganization().getZdbID())) {
               return true ; 
           }
        }
        return false;
    }

    public boolean hasPublicationAttribution(PublicationAttribution publicationAttribution) {
        for(PublicationAttribution aPublicationAttribution: getPublications()){
            if(aPublicationAttribution.getPublication().getZdbID().equals(publicationAttribution.getPublication().getZdbID())) {
                return true ;
            }
        }
        return false;
    }

    public MarkerAlias getAlias(String aliasString) {
        if(CollectionUtils.isEmpty(getAliases())) return null ;
        
        for(MarkerAlias aMarkerAlias: getAliases()){
            if(aMarkerAlias.getAlias().equalsIgnoreCase(aliasString)){
                return aMarkerAlias ;
            }
        }
        return null ;
    }

    public static enum Type {
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
        MRPHLNO("MRPHLNO"),
        MUTANT("MUTANT"),
        PAC("PAC"),
        PAC_END("PAC_END"),
        PTCONSTRCT("PTCONSTRCT"),
        RAPD("RAPD"),
        REGION("REGION"),
        SNP("SNP"),
        SSLP("SSLP"),
        STS("STS"),
        TGCONSTRCT("TGCONSTRCT"),
        TSCRIPT("TSCRIPT"),
        INDEL("INDEL");

        private final String value;

        private Type(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }

    }

    public static enum TypeGroup {
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
        REGION("REGION"),
        SEARCH_MK("SEARCH_MK"),
        SEARCH_MKSEG("SEARCH_MKSEG"),
        SEARCH_SEG("SEARCH_SEG"),
        SMALLSEG("SMALLSEG"),
        SSLP("SSLP"),
        STS("STS"),
        TGCONSTRUCT("TGCONSTRUCT"),
        TRANSCRIPT("TRANSCRIPT"),
        DEFICIENCY_TLOC_MARK("DEFICIENCY_TLOC_MARK");

        private final String value;

        private TypeGroup(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static TypeGroup getType(String type) {
            for (TypeGroup t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No run type of string " + type + " found.");
        }

    }

    public TreeSet<String> getLG() {
        TreeSet<String> lgSet = RepositoryFactory.getLinkageRepository().getLG(this);
        lgSet.remove("0");
        return lgSet;
    }

    public Set<DataNote> getDataNotes() {
        return dataNotes;
    }

    public void setDataNotes(Set<DataNote> dataNotes) {
        this.dataNotes = dataNotes;
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

}
