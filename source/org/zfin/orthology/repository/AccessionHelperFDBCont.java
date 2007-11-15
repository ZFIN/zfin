package org.zfin.orthology.repository;

/**
 * User: giles
 * Date: Sep 5, 2006
 * Time: 12:46:46 PM
 */

/**
 * Hibernate repository business object mapped to foreign_db_contains table
*/
public class AccessionHelperFDBCont {
    private String species;
    private AccessionHelperFDB FDBhelper;
    private String fdbcontID;

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public AccessionHelperFDB getFDBhelper() {
        return FDBhelper;
    }

    public void setFDBhelper(AccessionHelperFDB FDBhelper) {
        this.FDBhelper = FDBhelper;
    }

    public String getFdbcontID() {
        return fdbcontID;
    }

    public void setFdbcontID(String fdbcontID) {
        this.fdbcontID = fdbcontID;
    }
}
