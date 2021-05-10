package org.zfin.gwt.root.dto;

public class NcbiExternalReferenceDTO {

    private long ID;
    private String accessionNumber;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
