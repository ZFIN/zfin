package org.zfin.curation.presentation;

import java.util.Calendar;

public class IndexedStatusDTO {

    private String pubZdbID;
    private boolean indexed;
    private Calendar indexedDate;
    private PersonDTO indexer;

    public String getPubZdbID() {
        return pubZdbID;
    }

    public void setPubZdbID(String pubZdbID) {
        this.pubZdbID = pubZdbID;
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

    public PersonDTO getIndexer() {
        return indexer;
    }

    public void setIndexer(PersonDTO indexer) {
        this.indexer = indexer;
    }
}
