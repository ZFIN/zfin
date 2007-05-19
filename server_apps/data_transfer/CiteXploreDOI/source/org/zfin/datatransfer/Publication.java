/**
 *  Class Publication.
 *  Note:  This will be replaced with Publication in the java branch.  It is a wrapper around the Publication table.
 */
package org.zfin.datatransfer ; 

public class Publication {

    public Publication(){
    }

    private String zdbID = null ;
    private String accessionNumber = null;
    private String pubDOI  = null;


    
    /**
     * Get zdbID.
     *
     * @return zdbID as String.
     */
    public String getZdbID()
    {
        return zdbID;
    }
    
    /**
     * Set zdbID.
     *
     * @param zdbID the value to set.
     */
    public void setZdbID(String zdbID)
    {
        this.zdbID = zdbID;
    }
    
    /**
     * Get accessionNumber.
     *
     * @return accessionNumber as String.
     */
    public String getAccessionNumber()
    {
        return accessionNumber;
    }
    
    /**
     * Set accessionNumber.
     *
     * @param accessionNumber the value to set.
     */
    public void setAccessionNumber(String accessionNumber)
    {
        this.accessionNumber = accessionNumber;
    }
    
    /**
     * Get pubDOI.
     *
     * @return pubDOI as String.
     */
    public String getPubDOI()
    {
        return pubDOI;
    }
    
    /**
     * Set pubDOI.
     *
     * @param pubDOI the value to set.
     */
    public void setPubDOI(String pubDOI)
    {
        this.pubDOI = pubDOI;
    }
} 


