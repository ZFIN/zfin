/**
 *  Class DBLink.
 */
package org.zfin.sequence;


public abstract class DBLink {
    private String zdbID;
    private String accessionNumber;
    private Integer length;
    private ReferenceDatabase referenceDatabase;

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


//    public Set<RecordAttribution> getAttributions() {
//        return attributions;
//    }
//
//    public void setAttributions(Set<RecordAttribution> attributions) {
//        this.attributions = attributions;
//    }

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
        returnString += getAccessionNumber() + "\n" ;  
        returnString += getLength() + "\n" ;  
        returnString += getReferenceDatabase().getZdbID() + "\n" ;  
        return returnString ; 
    }
}


