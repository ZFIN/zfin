/**
 *  Class DBLink.
 */
package org.zfin.sequence;

import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.EntityAttribution;
import org.zfin.publication.Publication;
import org.zfin.sequence.blast.Origination;
import org.zfin.sequence.blast.Database;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.List;


public abstract class DBLink implements EntityAttribution {

    private final static Logger logger = Logger.getLogger(DBLink.class) ;

    private String zdbID;
    private String accessionNumber;
    private Integer length;
    private ReferenceDatabase referenceDatabase;
    private Set<PublicationAttribution> publications;
    private String dataZdbID ;
    private Integer version ;
    private String accessionNumberDisplay;

    public String getAccessionNumberDisplay() {
        return accessionNumberDisplay;
    }

    public void setAccessionNumberDisplay(String accessionNumberDisplay) {
        this.accessionNumberDisplay = accessionNumberDisplay;
    }


    /**
     * Get blastable databases according to FogBugz 4244
     From fogbugz 4244.
     For RNA seqs:

     > 25000 we should see MegaBlast and Ensembl
     < 25001 we should see ZFIN BLAST, NCBI BLAST, Ensembl, UCSC BLAT

     For Genomic seqs:

     > 200000 we should see MegaBlast only
     < 200000 and > 25000 MegaBLAST and Ensembl only
     < 200000 and < 25000 ZFIN BLAST, NCBI BLAST, Ensembl
     * @return List of blastable databases
     */
    public List<Database> getBlastableDatabases(){

        List<Database> blastableDatabases = referenceDatabase.getOrderedRelatedBlastDB() ;
        for(int i = 0 ; i < blastableDatabases.size() ; i++){
            Database database = blastableDatabases.get(i) ;
            if(referenceDatabase.getForeignDBDataType().getDataType()==ForeignDBDataType.DataType.RNA){
                // only ensembl and megablast allowed for large values
                if( length !=null && length > 25000
                        &&
                        ( database.getAbbrev()!=Database.AvailableAbbrev.ENSEMBL
                                ||
                                database.getAbbrev()!=Database.AvailableAbbrev.MEGA_BLAST
                        )
                        ){
                    blastableDatabases.remove(i) ;
                }
                else
                    // don't have megablast for small values
                    if(  length !=null && length <= 25000 && database.getAbbrev()==Database.AvailableAbbrev.MEGA_BLAST ){
                        blastableDatabases.remove(i) ;
                    }
            }
            else
            if(referenceDatabase.getForeignDBDataType().getDataType()==ForeignDBDataType.DataType.GENOMIC){
                // only megablast for very large
                if( length !=null && length > 200000 && database.getAbbrev()!=Database.AvailableAbbrev.MEGA_BLAST){
                    blastableDatabases.remove(i) ;
                }
                // only megablast and ensembl for medium
                else
                if( length !=null && length <= 200000 && length > 25000 &&
                        (  database.getAbbrev()!=Database.AvailableAbbrev.ENSEMBL
                                ||
                                database.getAbbrev()!=Database.AvailableAbbrev.MEGA_BLAST
                        )
                        ){
                    blastableDatabases.remove(i) ;
                }
                else
                // don't have megablast for small values
                if( length !=null && length <= 25000 && database.getAbbrev()==Database.AvailableAbbrev.MEGA_BLAST ){
                    blastableDatabases.remove(i) ;
                }
            }
            // do we need to handle other types?
        }
        return blastableDatabases ;
    }

    public String getUnits() {
        if (referenceDatabase.getForeignDBDataType().getDataType().equals(ForeignDBDataType.DataType.POLYPEPTIDE)) {
            return "aa";
        } else {
            return "bp";
        }
    }

    /**
     * @return Returns true if it would be possible to return this sequence internally.
     */
    public boolean isInternallyRetievableSequence(){
        if(referenceDatabase==null
                ||
                referenceDatabase.getPrimaryBlastDatabase()==null
                ){
            return false ;
        }

        boolean returnValue = referenceDatabase.getPrimaryBlastDatabase().getOrigination().getType()!= Origination.Type.GENERATED
                &&
                referenceDatabase.getPrimaryBlastDatabase().getOrigination().getType()!= Origination.Type.EXTERNAL ;
        logger.info("returnValue: "+ returnValue);
        return returnValue ;
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


    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            return getPublications().iterator().next().getPublication();
        }
        else{
            return null;
        }
    }

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

    public boolean equals(Object o){
        if(o instanceof DBLink){
            DBLink dbLink = (DBLink) o ;
            if( getZdbID()!=null &&  dbLink.getZdbID().equals(getZdbID()) ){
                return true ;
            }

            if( dbLink.getAccessionNumber().equals(dbLink.getAccessionNumber())
                    &&
                    dbLink.getReferenceDatabase().equals(dbLink.getReferenceDatabase())
                    ) {
                return true ;
            }
        }
        return false ;
    }

    public boolean isInDisplayGroup(DisplayGroup.GroupName groupName) {
        Set<DisplayGroup> displayGroups = referenceDatabase.getDisplayGroups() ;
        for(DisplayGroup displayGroup : displayGroups){
            if(displayGroup.getGroupName()==groupName){
                return true ;
            }
        }
        return false ;
    }


    public int hashCode() {
        int result = 1 ;
        result += (getZdbID() != null ? getZdbID().hashCode() : 0) * 29;
        result += (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0) * 19;
        result += (getReferenceDatabase() != null ? getReferenceDatabase().hashCode() : 0) * 17;
        return result;
    }


    public String toString(){
        String returnString =  "" ;
        returnString += getZdbID() + "\n" ;
        returnString += getDataZdbID() + "\n" ;
        returnString += getAccessionNumber() + "\n" ;
        returnString += getLength() + "\n" ;
        returnString += getReferenceDatabase().getZdbID() + "\n" ;
        return returnString ;
    }
}


