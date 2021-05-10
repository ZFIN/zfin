package org.zfin.marker.presentation;

/**
 */
public class ProteinEditBean {

    private String accession;
    private String refDBName;
    private String zdbID;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getRefDBName() {
        return refDBName;
    }

    public void setRefDBName(String refDBName) {
        this.refDBName = refDBName;
    }

    public String getZdbID() {
        if (zdbID == null) {
            return "ZDB-DBLINK-ACCESSION_IS_KEY";
        }
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}