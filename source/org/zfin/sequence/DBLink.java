/**
 *  Class DBLink.
 */
package org.zfin.sequence;

import org.zfin.infrastructure.RecordAttribution;

import java.util.Set;


public abstract class DBLink {
    private String zdbID;
    private String accessionNumber;
    private Integer length;
    private ReferenceDatabase referenceDatabase;
    private Set<RecordAttribution> attributions;

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


    public Set<RecordAttribution> getAttributions() {
        return attributions;
    }

    public void setAttributions(Set<RecordAttribution> attributions) {
        this.attributions = attributions;
    }
}


