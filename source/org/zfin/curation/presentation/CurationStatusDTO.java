package org.zfin.curation.presentation;

import java.util.Calendar;

public class CurationStatusDTO {

    private String pubZdbID;
    private Calendar closedDate;
    private boolean indexed;
    private Calendar indexedDate;
    private boolean curationAllowed;

    public boolean isCurationAllowed() {
        return curationAllowed;
    }

    public void setCurationAllowed(boolean curationAllowed) {
        this.curationAllowed = curationAllowed;
    }

    public String getPubZdbID() {
        return pubZdbID;
    }

    public void setPubZdbID(String pubZdbID) {
        this.pubZdbID = pubZdbID;
    }

    public Calendar getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Calendar completionDate) {
        this.closedDate = completionDate;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public Calendar getIndexedDate() {
        return indexedDate;
    }

    public void setIndexedDate(Calendar indexedDate) {
        this.indexedDate = indexedDate;
    }

}
