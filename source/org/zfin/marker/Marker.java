package org.zfin.marker;

import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.orthology.Orthologue;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.Person;
import org.zfin.mapping.MappedMarker;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.infrastructure.DataNote;

import java.util.*;
import java.io.Serializable;

/**
 * Domain model for the abstract marker object, which can be a gene, EST, CDNA, ...
 * ToDo: needs more modelling...
 */
public class Marker implements Serializable, Comparable {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String abbreviationOrder;
    private Set<ExpressionExperiment> probeExpressionExperiments;
    private Set<ExpressionExperiment> expressionExperiments;
    private Set<Publication> publications;
    private HashMap<String, List<Publication>> pubsPerAnatomy;
    private Set<Figure> figures;
    private MarkerFamilyName geneFamilyName;
    private Set<Orthologue> orthologues;
    private Set<MarkerRelationship> firstMarkerRelationships;    //  where this marker = "mrel_mrkr_1_zdb_id" in mrel
    private Set<MarkerRelationship> secondMarkerRelationships;   //  where this marker = "mrel_mrkr_2_zdb_id" in mrel 
    private MarkerType markerType;
    private Set<MarkerHistory> markerHistory;
    private Set<MappedMarker> directPanelMappings;
    private Person owner;
    private String comments;
    private Set<MarkerDBLink> dbLinks;
    private Set<MarkerAlias> aliases;
    private Set<DataNote> dataNotes;

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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Set<MarkerDBLink> getDbLinks() {
        return dbLinks;
    }

    public void setDbLinks(Set<MarkerDBLink> dbLinks) {
        this.dbLinks = dbLinks;
    }

    public Set<MarkerAlias> getAliases() {
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

    public Set<Publication> getPublications() {
        if (expressionExperiments == null)
            return null;
        Set<Publication> pubs = new HashSet<Publication>();
        for (ExpressionExperiment exp : expressionExperiments) {
            Publication publication = exp.getPublication();
            pubs.add(publication);
        }
        return pubs;
    }

    /**
     * obtain a list of publications that are expressed in this gene and
     * the given anatomical structure.
     *
     * @param aoZdbID
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
     */
    public int getNumberOfPublications() {
        if (expressionExperiments == null)
            return 0;
        return getPublications().size();
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setGeneFamilyName(MarkerFamilyName geneFamilyName) {
        this.geneFamilyName = geneFamilyName;
    }

    public MarkerFamilyName getGeneFamilyName() {
        return geneFamilyName;
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
        sb.append("name: " + name);
        sb.append("\r\n");
        sb.append("symbol: " + abbreviation);
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
        return getAbbreviationOrder().compareTo(((Marker) otherMarker).getAbbreviationOrder());
    }

    public Set<MappedMarker> getDirectPanelMappings() {
        return directPanelMappings;
    }

    public void setDirectPanelMappings(Set<MappedMarker> directPanelMappings) {
        this.directPanelMappings = directPanelMappings;
    }

    public void addPublication(Publication pub) {
        if (publications == null)
            publications = new HashSet<Publication>();
        publications.add(pub);
    }

    public static enum Type {
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
        TGCONSTRCT("TGCONSTRCT");

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
        BAC("BAC"),
        BAC_END("BAC_END"),
        CAN_HAVE_MRPHLN("CAN_HAVE_MRPHLN"),
        CDNA("CDNA"),
        CLONE("CLONE"),
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
        TGCONSTRUCT("TGCONSTRUCT");

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
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        TreeSet<String> lgSet = markerRepository.getLG(this);
        lgSet.remove("0");
        return lgSet;
    }

    public Set<DataNote> getDataNotes() {
        return dataNotes;
    }

    public void setDataNotes(Set<DataNote> dataNotes) {
        this.dataNotes = dataNotes;
    }

}
