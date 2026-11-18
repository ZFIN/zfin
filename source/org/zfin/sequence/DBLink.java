package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DiscriminatorFormula;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.Origination;
import org.zfin.sequence.gff.Assembly;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE get_obj_type(dblink_linked_recid) " + 
        " WHEN 'ORTHO' THEN 'ORTH' " + 
        " WHEN 'TERM' THEN 'TERM' " +
        " WHEN 'TSCRIPT' THEN 'TSCR' " + 
        " WHEN 'ALT' THEN 'ALT' " + 
        " ELSE 'MARK' " + 
        " END"
)
@Table(name = "db_link")
public abstract class DBLink implements EntityAttribution, EntityZdbID {

    private final static Logger logger = LogManager.getLogger(DBLink.class);

    @JsonView(View.SequenceAPI.class)
    @Id
    @Column(name = "dblink_zdb_id", nullable = false)
    @GeneratedValue(generator = "zdbIdGeneratorForDBLink")
    @org.hibernate.annotations.GenericGenerator(
            name = "zdbIdGeneratorForDBLink",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "DBLINK"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            }
    )
    private String zdbID;

    @JsonView(View.API.class)
    @Column(name = "dblink_acc_num", nullable = false)
    private String accessionNumber;

    @ManyToOne
    @JoinColumn(name = "dblink_acc_num", referencedColumnName = "accbk_acc_num", insertable = false, updatable = false)
    private Accession accession;

    @JsonView(View.API.class)
    @Column(name = "dblink_length")
    private Integer length;

    @JsonView({View.MarkerRelationshipAPI.class, View.SequenceDetailAPI.class})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dblink_fdbcont_zdb_id", nullable = false)
    private ReferenceDatabase referenceDatabase;

    @OneToMany(mappedBy = "dataZdbID", fetch = FetchType.LAZY)
    private Set<PublicationAttribution> publications;

    @Column(name = "dblink_linked_recid", nullable = false, insertable = false, updatable = false)
    private String dataZdbID;

    @Transient
    private Integer version;

    @JsonView(View.MarkerRelationshipAPI.class)
    @Column(name = "dblink_acc_num_display")
    private String accessionNumberDisplay;

    @JsonView(View.SequenceDetailAPI.class)
    @Column(name = "dblink_info")
    private String linkInfo;

    @JsonView(View.SequenceAPI.class)
    @Transient
    private Sequence sequence;

    // Not @JsonView-annotated: Assembly.marker (mappedBy="assemblies") has no @JsonView of its
    // own, so with Jackson's default view inclusion, serializing this collection directly would
    // pull in full Marker object graphs through that back-reference. MarkerDBLink.getAssemblyNames()
    // exposes what the API needs as plain Strings instead. @BatchSize: a gene's Sequences table
    // reads this for every row, so initialize up to 100 rows' collections per query, not one each.
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    @JoinTable(name = "db_link_assembly",
            joinColumns = @JoinColumn(name = "dbla_dblink_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "dbla_a_pk_id"))
    private Set<Assembly> assemblies;

    @JsonView(View.SequenceAPI.class)
    @JsonProperty("type")
    public String getSequenceType() {
        return referenceDatabase.getForeignDBDataType().getDataType().toString();
    }

    /*
        @JsonView(View.SequenceAPI.class)
        @JsonProperty("lengthDisplay")
    */
    public String getHRLength() {
        if (length != null)
            return NumberFormat.getInstance().format(length);
        return "0";
    }

    /**
     * Get blastable databases according to FogBugz 4244, without Ensembl (ZFIN-10602).
     * For RNA and genomic sequences:
     * <p/>
     * > 25000 we should see MegaBLAST only
     * <= 25000 we should see every related tool except MegaBLAST (ZFIN BLAST, NCBI BLAST, UCSC BLAT)
     * <p/>
     * Other data types (e.g. polypeptide) are not filtered.
     *
     * @return List of blastable databases
     */
    @JsonView(View.SequenceAPI.class)
    public List<Database> getBlastableDatabases() {

        List<Database> blastableDatabases = referenceDatabase.getOrderedRelatedBlastDB();
        ForeignDBDataType.DataType dataType = referenceDatabase.getForeignDBDataType().getDataType();
        if (length == null
            || (dataType != ForeignDBDataType.DataType.RNA && dataType != ForeignDBDataType.DataType.GENOMIC)) {
            return blastableDatabases;
        }

        boolean largeSequence = length > 25000;
        // large sequences: only MegaBLAST; small sequences: everything but MegaBLAST
        blastableDatabases.removeIf(database ->
            (database.getAbbrev() == Database.AvailableAbbrev.MEGA_BLAST) != largeSequence);
        return blastableDatabases;
    }

    @JsonView(View.SequenceAPI.class)
    public String getUnits() {
        if (referenceDatabase.getForeignDBDataType().getDataType().equals(ForeignDBDataType.DataType.POLYPEPTIDE)) {
            return "aa";
        } else {
            return "nt";
        }
    }

    /**
     * @return Returns true if it would be possible to return this sequence internally.
     */
    public boolean isInternallyRetievableSequence() {
        if (referenceDatabase == null
            ||
            referenceDatabase.getPrimaryBlastDatabase() == null
        ) {
            return false;
        }

        boolean returnValue = referenceDatabase.getPrimaryBlastDatabase().getOrigination().getType() != Origination.Type.GENERATED
                              &&
                              referenceDatabase.getPrimaryBlastDatabase().getOrigination().getType() != Origination.Type.EXTERNAL;
        logger.info("returnValue: " + returnValue);
        return returnValue;
    }


    @JsonView(View.SequenceAPI.class)
    public String getPublicationIds() {
        return String.join(",", getPublicationIdsAsList());
    }

    public List<String> getPublicationIdsAsList() {
        return publications.stream().map(publicationAttribution -> publicationAttribution.getPublication().getZdbID()).toList();
    }

    @JsonView(View.SequenceAPI.class)
    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            return getPublications().iterator().next().getPublication();
        } else {
            return null;
        }
    }

    @JsonView(View.SequenceAPI.class)
    public int getPublicationCount() {
        if (publications == null)
            return 0;
        else
            return publications.size();
    }

//    public Set<RecordAttribution> getAttributions() {
//        return attributions;
//    }
//
//    public void setRelatedEntities(Set<RecordAttribution> attributions) {
//        this.attributions = attributions;
//    }

    public boolean equals(Object o) {
        if (o instanceof DBLink dbLink) {
            if (getZdbID() != null && dbLink.getZdbID().equals(getZdbID())) {
                return true;
            }

            if (getAccessionNumber().equals(dbLink.getAccessionNumber())
                &&
                getReferenceDatabase().equals(dbLink.getReferenceDatabase())
            ) {
                return true;
            }
        }
        return false;
    }

    public boolean isInDisplayGroup(DisplayGroup.GroupName groupName) {
        Set<DisplayGroup> displayGroups = referenceDatabase.getDisplayGroups();
        for (DisplayGroup displayGroup : displayGroups) {
            if (displayGroup.getGroupName() == groupName) {
                return true;
            }
        }
        return false;
    }


    public int hashCode() {
        int result = 1;
        result += (getZdbID() != null ? getZdbID().hashCode() : 0) * 29;
        result += (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0) * 19;
        result += (getReferenceDatabase() != null ? getReferenceDatabase().hashCode() : 0) * 17;
        return result;
    }

    public String toString() {
        String returnString = "";
        returnString += getZdbID() + "\n";
        returnString += getDataZdbID() + "\n";
        returnString += getAccessionNumber() + "\n";
        returnString += getLength() + "\n";
        returnString += getReferenceDatabase().getZdbID() + "\n";
        return returnString;
    }

    @Override
    public String getAbbreviation() {
        return accessionNumber;
    }

    @Override
    public String getAbbreviationOrder() {
        return accessionNumber;
    }

    @Override
    public String getEntityType() {
        return "Accession Number";
    }

    @Override
    public String getEntityName() {
        return accessionNumber;
    }

    @JsonView(View.SequenceAPI.class)
    @JsonProperty("url")
    public String getUrl() {
        String prefix = referenceDatabase.getForeignDB().getDbUrlPrefix();
        if (prefix == null || prefix.isEmpty()) {
            return null;
        }
        return prefix + accessionNumber;
    }

    @JsonView(View.SequenceAPI.class)
    @JsonProperty("displayName")
    public String getDisplayName() {
        return referenceDatabase.getForeignDB().getDisplayName() + ":" + accessionNumber;
    }

    public void addPublicationAttributions(Set<PublicationAttribution> publications) {
        if (this.publications == null)
            this.publications = new HashSet<>();
        this.publications.addAll(publications);
    }

    public boolean isValidAccessionFormat() {
        return getReferenceDatabase().isValidAccessionFormat(this.getAccessionNumber());
    }
}
