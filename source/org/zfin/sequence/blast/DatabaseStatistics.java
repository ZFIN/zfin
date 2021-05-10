package org.zfin.sequence.blast;

import java.util.Date;

/**
 * Holds blast database statistics.
 */
public class DatabaseStatistics {

    public static final int BAD_DATABASE  = -1;

    private int numSequences = BAD_DATABASE;
    private int numAccessions = BAD_DATABASE ;
    private Date creationDate ;
    private Date modifiedDate ;


    public int getNumSequences() {
        return numSequences;
    }

    public void setNumSequences(int numSequences) {
        this.numSequences = numSequences;
    }

    public int getNumAccessions() {
        return numAccessions;
    }

    public void setNumAccessions(int numAccessions) {
        this.numAccessions = numAccessions;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isSet() {
        return
                creationDate!=null
                        &&
                        modifiedDate!=null
                        &&
                        numAccessions!=BAD_DATABASE;
    }
}
