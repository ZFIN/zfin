package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityAttribution;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.blast.Origination;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public abstract class DBLink implements EntityAttribution, EntityZdbID {

    private final static Logger logger = LogManager.getLogger(DBLink.class);

    @JsonView(View.SequenceAPI.class)
    private String zdbID;
    @JsonView(View.API.class)
    private String accessionNumber;
    @JsonView(View.API.class)
    private Integer length;
    @JsonView({View.MarkerRelationshipAPI.class, View.SequenceDetailAPI.class})
    private ReferenceDatabase referenceDatabase;
    private Set<PublicationAttribution> publications;
    private String dataZdbID;
    private Integer version;
    @JsonView(View.MarkerRelationshipAPI.class)
    private String accessionNumberDisplay;
    @JsonView(View.SequenceDetailAPI.class)
    private String linkInfo;
    @JsonView(View.SequenceAPI.class)
    private Sequence sequence;

    public String getLinkInfo() {
        return linkInfo;
    }

    public void setLinkInfo(String linkInfo) {
        this.linkInfo = linkInfo;
    }

    public String getAccessionNumberDisplay() {
        return accessionNumberDisplay;
    }

    public void setAccessionNumberDisplay(String accessionNumberDisplay) {
        this.accessionNumberDisplay = accessionNumberDisplay;
    }

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
     * Get blastable databases according to FogBugz 4244
     * From fogbugz 4244.
     * For RNA seqs:
     * <p/>
     * > 25000 we should see MegaBlast and Ensembl
     * < 25001 we should see ZFIN BLAST, NCBI BLAST, Ensembl, UCSC BLAT
     * <p/>
     * For Genomic seqs:
     * <p/>
     * > 200000 we should see MegaBlast only
     * < 200000 and > 25000 MegaBLAST and Ensembl only
     * < 200000 and < 25000 ZFIN BLAST, NCBI BLAST, Ensembl
     *
     * @return List of blastable databases
     */
    @JsonView(View.SequenceAPI.class)
    public List<Database> getBlastableDatabases() {

        List<Database> blastableDatabases = referenceDatabase.getOrderedRelatedBlastDB();

        for (Iterator<Database> iterator = blastableDatabases.iterator(); iterator.hasNext(); ) {
            Database database = iterator.next();
            if (referenceDatabase.getForeignDBDataType().getDataType() == ForeignDBDataType.DataType.RNA) {
                // only ensembl and megablast allowed for large values
                if (length != null && length > 25000
                    &&
                    (database.getAbbrev() != Database.AvailableAbbrev.ENSEMBL
                     &&
                     database.getAbbrev() != Database.AvailableAbbrev.MEGA_BLAST
                    )
                ) {
                    iterator.remove();
//                    blastableDatabases.remove(i);
                } else
                    // don't have megablast for small values
                    if (length != null && length <= 25000 && database.getAbbrev() == Database.AvailableAbbrev.MEGA_BLAST) {
                        iterator.remove();
//                        blastableDatabases.remove(i);
                    }
            } else if (referenceDatabase.getForeignDBDataType().getDataType() == ForeignDBDataType.DataType.GENOMIC) {
                // only megablast for very large
                if (length != null && length > 200000 && database.getAbbrev() != Database.AvailableAbbrev.MEGA_BLAST) {
                    iterator.remove();
//                    blastableDatabases.remove(i);
                }
                // only megablast and ensembl for medium
                else if (length != null && length <= 200000 && length > 25000 &&
                         (database.getAbbrev() != Database.AvailableAbbrev.ENSEMBL
                          &&
                          database.getAbbrev() != Database.AvailableAbbrev.MEGA_BLAST
                         )
                ) {
                    iterator.remove();
//                    blastableDatabases.remove(i);
                } else
                    // don't have megablast for small values
                    if (length != null && length <= 25000 && database.getAbbrev() == Database.AvailableAbbrev.MEGA_BLAST) {
                        iterator.remove();
//                        blastableDatabases.remove(i);
                    }
            }
            // do we need to handle other types?
        }
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


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Set<PublicationAttribution> getPublications() {
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
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

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

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
        return referenceDatabase.getForeignDB().getDbUrlPrefix() + accessionNumber;
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


